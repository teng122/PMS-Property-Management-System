package com.smarthotel.booking_service.service;

import com.smarthotel.booking_service.client.RoomClient;
import com.smarthotel.booking_service.dto.external.RoomDto;
import com.smarthotel.booking_service.dto.external.InvoiceDto;
import com.smarthotel.booking_service.dto.external.RoomStatusUpdateRequest;
import com.smarthotel.booking_service.dto.request.BookingRequest;
import com.smarthotel.booking_service.dto.response.BookingResponse;
import com.smarthotel.booking_service.dto.response.PreCheckoutSummaryResponse;
import com.smarthotel.booking_service.entity.Booking;
import com.smarthotel.booking_service.entity.BookingStatus;
import com.smarthotel.common_shared.model.RoomStatus;
import com.smarthotel.booking_service.exception.*;
import com.smarthotel.booking_service.messaging.BookingEventPublisher;
import com.smarthotel.booking_service.repository.BookingRepository;
import com.smarthotel.common_shared.event.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Service xử lý toàn bộ logic nghiệp vụ liên quan đến Booking.
 * Hỗ trợ các luồng Reservation, Check-in, Checkout và Billing.
 */
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomClient roomClient;
    private final BookingEventPublisher bookingEventPublisher;

    @Autowired
    @org.springframework.context.annotation.Lazy
    private BookingService self;

    // ==========================================
    // 1. NHÓM ĐẶT PHÒNG (RESERVATION METHODS)
    // ==========================================

    /**
     * Tạo đơn đặt phòng trực tuyến (Online Reservation) qua Saga workflow.
     * Kiểm tra thông tin phòng, tính toán tổng số tiền phòng và xuất bản BookingCreatedEvent.
     */
    @Transactional
    public Booking createOnlineBooking(BookingRequest bookingRequest) {
        // Khóa độc quyền phòng ở mức cơ sở dữ liệu để ngăn race condition (2 người đặt cùng lúc) mà không gây nghẽn pool kết nối
        if (!bookingRepository.tryAcquireRoomLock(bookingRequest.getRoomId())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT,
                    "Phòng này đang được một khách hàng khác thực hiện thao tác đặt chỗ. Vui lòng thử lại sau 5 phút!"
            );
        }

        // Kiểm tra trùng lịch đặt phòng cục bộ ngay lập tức (Fail-Fast)
        boolean isAvailable = checkAvailability(
                bookingRequest.getRoomId(),
                null,
                bookingRequest.getCheckInDate().atTime(14, 0),
                bookingRequest.getCheckOutDate().atTime(12, 0)
        );
        if (!isAvailable) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT,
                    "Phòng đã có khách khác đặt trùng lịch từ trước! Vui lòng thử lại sau 5 phút!"
            );
        }

        RoomDto room = roomClient.getRoomById(bookingRequest.getRoomId());
        if (room == null) {
            throw new BookingNotFoundException("Không tìm thấy thông tin phòng với ID: " + bookingRequest.getRoomId());
        }

        if (!bookingRequest.getCheckOutDate().isAfter(bookingRequest.getCheckInDate())) {
            throw new IllegalArgumentException("Ngày trả phòng (check-out) phải sau ngày nhận phòng (check-in) ít nhất 1 ngày!");
        }

        long days = ChronoUnit.DAYS.between(bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate());
        if (days <= 0) {
            days = 1; // Tối thiểu ở 1 ngày
        }
        java.math.BigDecimal totalAmount = room.getPrice().multiply(java.math.BigDecimal.valueOf(days));
        java.math.BigDecimal depositAmount = totalAmount.multiply(java.math.BigDecimal.valueOf(0.5)).setScale(2, java.math.RoundingMode.HALF_UP); // 50% đặt cọc

        Booking booking = Booking.builder()
                .customerId(bookingRequest.getCustomerId())
                .roomId(bookingRequest.getRoomId())
                .checkInDate(bookingRequest.getCheckInDate().atTime(14, 0)) // Check-in lúc 14:00
                .checkOutDate(bookingRequest.getCheckOutDate().atTime(12, 0)) // Check-out lúc 12:00
                .status(BookingStatus.PENDING)
                .totalAmount(totalAmount)
                .depositAmount(depositAmount)
                .isDepositPaid(false)
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        BookingCreatedEvent event = BookingCreatedEvent.builder()
                .eventId(UUID.randomUUID())
                .bookingId(savedBooking.getId())
                .roomId(savedBooking.getRoomId())
                .checkInDate(savedBooking.getCheckInDate())
                .checkOutDate(savedBooking.getCheckOutDate())
                .build();

        bookingEventPublisher.publishBookingCreated(event);

        return savedBooking;
    }

    // ==========================================
    // 2. NHÓM NHẬN PHÒNG (CHECK-IN METHODS)
    // ==========================================

    /**
     * Nhận phòng trực tiếp (Walk-in Check-in) tại quầy cho khách vãng lai không đặt trước.
     * Phòng phải ở trạng thái AVAILABLE. Booking được chuyển ngay sang trạng thái CHECKED_IN.
     */
    @Transactional
    public Booking performWalkInCheckIn(Booking walkInData) {
        // Khóa độc quyền phòng ở mức cơ sở dữ liệu để ngăn race condition nhận phòng trùng
        if (!bookingRepository.tryAcquireRoomLock(walkInData.getRoomId())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT,
                    "Phòng này đang được một khách hàng khác thực hiện thao tác nhận phòng. Vui lòng thử lại sau 5 phút!"
            );
        }

        RoomDto room = roomClient.getRoomById(walkInData.getRoomId());
        if (room == null) {
            throw new BookingNotFoundException("Không tìm thấy thông tin phòng từ Room Service!");
        }
        if (!RoomStatus.AVAILABLE.name().equalsIgnoreCase(room.getStatus())) {
            throw new InvalidRoomStatusException("Phòng số " + room.getRoomNumber() + " hiện tại không trống để check-in!");
        }
        if (walkInData.getCheckOutDate() == null) {
            throw new IllegalArgumentException("Ngày check-out không được trống!");
        }
        if (!walkInData.getCheckOutDate().toLocalDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày trả phòng (check-out) phải sau ngày nhận phòng (hôm nay) ít nhất 1 ngày!");
        }

        long days = ChronoUnit.DAYS.between(LocalDate.now(), walkInData.getCheckOutDate().toLocalDate());
        if (days <= 0) {
            days = 1;
        }
        java.math.BigDecimal totalAmount = room.getPrice().multiply(java.math.BigDecimal.valueOf(days));
        walkInData.setTotalAmount(totalAmount);

        walkInData.setCheckInDate(LocalDateTime.now());
        walkInData.setCheckOutDate(walkInData.getCheckOutDate().toLocalDate().atTime(12, 0));
        walkInData.setStatus(BookingStatus.CHECKED_IN);
        walkInData.setDepositAmount(java.math.BigDecimal.ZERO);
        walkInData.setIsDepositPaid(true);

        Booking savedBooking = bookingRepository.save(walkInData);

        roomClient.updateRoomStatus(walkInData.getRoomId(), new RoomStatusUpdateRequest(RoomStatus.OCCUPIED.name()));

        return savedBooking;
    }

    /**
     * Làm thủ tục nhận phòng (Check-in) cho khách đã đặt phòng trước đó.
     * Thực hiện kiểm tra quyền sở hữu và gọi cập nhật trạng thái phòng vật lý ngoài transaction (tránh nghẽn HikariCP).
     */
    public Booking performCheckIn(UUID bookingId) {
        Booking booking = bookingRepository.findByIdOrThrow(bookingId);

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new InvalidBookingStatusException("Đơn đặt phòng này không ở trạng thái hợp lệ để Check-in!");
        }

        // Cập nhật Database cục bộ thông qua proxy transactional cực ngắn
        Booking savedBooking = self.executeUpdateCheckIn(bookingId);

        // Cập nhật trạng thái phòng qua Feign ngoài transaction (không giữ connection DB)
        try {
            roomClient.updateRoomStatus(booking.getRoomId(), new RoomStatusUpdateRequest(RoomStatus.OCCUPIED.name()));
        } catch (Exception e) {
            // Hoàn tác cập nhật cục bộ nếu Feign lỗi
            self.executeRevertCheckIn(bookingId);
            throw new RuntimeException("Lỗi khi kết nối Room Service để nhận phòng: " + e.getMessage(), e);
        }

        return savedBooking;
    }

    /**
     * Phương thức phụ trợ được bọc Transaction ngắn để ghi nhận Check-in xuống Database.
     */
    @Transactional
    public Booking executeUpdateCheckIn(UUID bookingId) {
        Booking booking = bookingRepository.findByIdOrThrow(bookingId);
        booking.setCheckInDate(LocalDateTime.now());
        booking.setStatus(BookingStatus.CHECKED_IN);
        return bookingRepository.save(booking);
    }

    /**
     * Phương thức phụ trợ để khôi phục trạng thái nếu check-in thất bại.
     */
    @Transactional
    public void executeRevertCheckIn(UUID bookingId) {
        Booking booking = bookingRepository.findByIdOrThrow(bookingId);
        booking.setCheckInDate(null);
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
    }

    // ==========================================
    // 3. NHÓM THANH TOÁN & TRẢ PHÒNG (CHECK-OUT & BILLING METHODS)
    // ==========================================

    /**
     * Lấy tóm tắt chi tiết hóa đơn (tiền phòng + dịch vụ) và trạng thái thanh toán hiện tại của khách trước khi Check-out.
     */
    @Transactional(readOnly = true)
    public PreCheckoutSummaryResponse getPreCheckoutSummary(UUID bookingId) {
        Booking booking = bookingRepository.findByIdOrThrow(bookingId);

        RoomDto room = roomClient.getRoomById(booking.getRoomId());
        String roomNumber = (room != null) ? room.getRoomNumber() : "N/A";

        String customerName = "Khách hàng #" + booking.getCustomerId().toString().substring(0, 8); // Tinh gọn không gọi identity-service

        return PreCheckoutSummaryResponse.builder()
                .bookingId(bookingId)
                .customerName(customerName)
                .roomNumber(roomNumber)
                .invoice(null) // Lược bỏ Feign client gọi billing-service chéo
                .paymentStatus("PENDING_CHECKOUT")
                .build();
    }

    /**
     * Thực hiện thủ tục trả phòng (Check-out) cho khách hàng.
     * Chuyển đổi trạng thái đơn đặt phòng sang CHECKED_OUT và bắn sự kiện CheckoutStartedEvent không đồng bộ qua Kafka.
     */
    public Booking performCheckOut(UUID bookingId) {
        Booking booking = bookingRepository.findByIdOrThrow(bookingId);

        if (booking.getStatus() != BookingStatus.CHECKED_IN) {
            throw new InvalidBookingStatusException("Đơn đặt phòng này không ở trạng thái hợp lệ để Check-out!");
        }

        RoomDto room = roomClient.getRoomById(booking.getRoomId());
        if (room == null) {
            throw new BookingNotFoundException("Không tìm thấy thông tin phòng từ Room Service!");
        }

        long daysBetween = ChronoUnit.DAYS.between(booking.getCheckInDate().toLocalDate(), LocalDate.now());
        if (daysBetween <= 0) {
            daysBetween = 1;
        }

        // roomCharge = giá phòng một đêm * số ngày thực tế
        java.math.BigDecimal roomCharge = room.getPrice().multiply(java.math.BigDecimal.valueOf(daysBetween));

        // Cập nhật database local qua transaction ngắn
        Booking updatedBooking = self.executeUpdateCheckOut(bookingId);

        // Bắn event CheckoutStartedEvent qua Kafka
        try {
            CheckoutStartedEvent event = CheckoutStartedEvent.builder()
                    .eventId(UUID.randomUUID())
                    .bookingId(updatedBooking.getId())
                    .roomId(updatedBooking.getRoomId())
                    .customerId(updatedBooking.getCustomerId())
                    .roomCharge(roomCharge)
                    .depositAmount(updatedBooking.getDepositAmount()) // Bổ sung depositAmount vào Event
                    .timestamp(LocalDateTime.now())
                    .build();
            bookingEventPublisher.publishCheckoutStarted(event);
        } catch (Exception e) {
            // Hoàn tác cập nhật cục bộ nếu Kafka lỗi
            self.executeRevertCheckOut(bookingId, booking.getCheckOutDate());
            throw new RuntimeException("Lỗi khi gửi sự kiện Check-out lên hệ thống: " + e.getMessage(), e);
        }

        return updatedBooking;
    }

    /**
     * Phương thức phụ trợ được bọc Transaction ngắn để ghi nhận Check-out xuống Database.
     */
    @Transactional
    public Booking executeUpdateCheckOut(UUID bookingId) {
        Booking booking = bookingRepository.findByIdOrThrow(bookingId);
        booking.setCheckOutDate(LocalDateTime.now());
        booking.setStatus(BookingStatus.CHECKED_OUT);
        return bookingRepository.save(booking);
    }

    /**
     * Phương thức phụ trợ để khôi phục trạng thái nếu check-out thất bại.
     */
    @Transactional
    public void executeRevertCheckOut(UUID bookingId, LocalDateTime originalCheckOutDate) {
        Booking booking = bookingRepository.findByIdOrThrow(bookingId);
        booking.setCheckOutDate(originalCheckOutDate);
        booking.setStatus(BookingStatus.CHECKED_IN);
        bookingRepository.save(booking);
    }

    // ==========================================
    // 4. NHÓM QUẢN LÝ & TRA CỨU HỆ THỐNG
    // ==========================================

    /**
     * Tra cứu chi tiết đơn đặt phòng bằng ID.
     */
    @Transactional(readOnly = true)
    public Booking getBookingById(UUID id) {
        return bookingRepository.findByIdOrThrow(id);
    }

    /**
     * Truy vấn toàn bộ danh sách đặt phòng.
     */
    @Transactional(readOnly = true)
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    /**
     * Lấy danh sách đơn đặt phòng của một khách hàng (dùng cho màn "Booking của tôi").
     */
    @Transactional(readOnly = true)
    public List<Booking> getBookingsByCustomerId(UUID customerId) {
        return bookingRepository.findByCustomerId(customerId);
    }

    /**
     * Cập nhật thông tin chi tiết đơn đặt phòng.
     */
    @Transactional
    public Booking updateBooking(UUID id, Booking updatedData) {
        Booking existingBooking = getBookingById(id);

        existingBooking.setCustomerId(updatedData.getCustomerId());
        existingBooking.setCheckInDate(updatedData.getCheckInDate());
        existingBooking.setCheckOutDate(updatedData.getCheckOutDate());
        existingBooking.setStatus(updatedData.getStatus());

        return bookingRepository.save(existingBooking);
    }

    /**
     * Xóa đơn đặt phòng khỏi Database.
     */
    @Transactional
    public void deleteBooking(UUID id) {
        Booking existingBooking = getBookingById(id);
        bookingRepository.delete(existingBooking);
    }

    /**
     * Tìm danh sách ID phòng bận trong khoảng thời gian (Phục vụ truy vấn chéo từ Room Service).
     */
    @Transactional(readOnly = true)
    public List<UUID> getActiveRoomIds(LocalDate checkIn, LocalDate checkOut) {
        LocalDateTime start = checkIn.atTime(14, 0);
        LocalDateTime end = checkOut.atTime(12, 0);
        return bookingRepository.findActiveRoomIds(start, end);
    }

    @Transactional(readOnly = true)
    public List<RoomDto> searchAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("Ngày nhận phòng và trả phòng không được để trống!");
        }
        if (checkIn.isAfter(checkOut)) {
            throw new IllegalArgumentException("Ngày trả phòng (check-out) phải ở sau ngày nhận phòng (check-in)!");
        }

        // Lấy danh sách ID phòng bận cục bộ
        List<UUID> activeRoomIds = getActiveRoomIds(checkIn, checkOut);

        // Lấy toàn bộ phòng vật lý từ Room Service (Feign call ngoài transaction)
        List<RoomDto> allRooms = roomClient.getAllRooms();

        // Lọc các phòng chưa bận
        return allRooms.stream()
                .filter(room -> !activeRoomIds.contains(room.getId()))
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Booking getActiveBookingByRoomId(UUID roomId) {
        return bookingRepository.findByRoomIdAndStatus(roomId, BookingStatus.CHECKED_IN)
                .orElse(null);
    }

    @Transactional
    public Booking payDeposit(UUID bookingId, java.math.BigDecimal amount) {
        Booking booking = bookingRepository.findByIdOrThrow(bookingId);
        if (booking.getStatus() != BookingStatus.AWAITING_DEPOSIT) {
            throw new InvalidBookingStatusException("Đơn đặt phòng không ở trạng thái chờ đặt cọc!");
        }
        if (amount.compareTo(booking.getDepositAmount()) < 0) {
            throw new IllegalArgumentException("Số tiền đặt cọc không đủ! Yêu cầu tối thiểu: " + booking.getDepositAmount());
        }
        booking.setIsDepositPaid(true);
        booking.setStatus(BookingStatus.CONFIRMED);
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking markNoShow(UUID bookingId) {
        Booking booking = bookingRepository.findByIdOrThrow(bookingId);
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new InvalidBookingStatusException("Chỉ đơn đặt phòng đã xác nhận (CONFIRMED) mới có thể đánh dấu No-Show!");
        }
        booking.setStatus(BookingStatus.NO_SHOW);
        return bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public boolean checkAvailability(UUID roomId, UUID bookingId, LocalDateTime checkIn, LocalDateTime checkOut) {
        long count = bookingRepository.countConflictingBookings(roomId, bookingId, checkIn, checkOut);
        return count == 0;
    }
}