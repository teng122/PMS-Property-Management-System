package com.smarthotel.booking_service.service;

import com.smarthotel.booking_service.client.RoomClient;
import com.smarthotel.booking_service.dto.external.RoomDto;
import com.smarthotel.booking_service.dto.external.RoomStatusUpdateRequest;
import com.smarthotel.booking_service.dto.request.BookingRequest;
import com.smarthotel.booking_service.dto.response.BookingResponse;
import com.smarthotel.booking_service.entity.Booking;
import com.smarthotel.booking_service.entity.BookingStatus;
import com.smarthotel.booking_service.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomClient roomClient; // Tiêm client vào đây

    private BookingResponse mapToResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .roomId(booking.getRoomId())
                .customerName(booking.getCustomerName())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .status(booking.getStatus())
                .build();
    }

    // 1. Lấy toàn bộ danh sách đơn đặt phòng
    @Transactional(readOnly = true)
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    // 2. Tìm đơn đặt phòng theo ID
    @Transactional(readOnly = true)
    public Booking getBookingById(UUID id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đặt phòng với ID: " + id));
    }

    // 3. Cập nhật thông tin đơn đặt phòng
    @Transactional
    public Booking updateBooking(UUID id, Booking updatedData) {
        Booking existingBooking = getBookingById(id);

        // Cập nhật các trường cho phép sửa
        existingBooking.setCustomerName(updatedData.getCustomerName());
        existingBooking.setCheckInDate(updatedData.getCheckInDate());
        existingBooking.setCheckOutDate(updatedData.getCheckOutDate());
        existingBooking.setStatus(updatedData.getStatus());
        // Không set lại id, roomId (nếu nghiệp vụ không cho đổi phòng trực tiếp) và createdAt

        return bookingRepository.save(existingBooking);
    }

    // 4. Xóa đơn đặt phòng
    @Transactional
    public void deleteBooking(UUID id) {
        Booking existingBooking = getBookingById(id);
        bookingRepository.delete(existingBooking);
    }

    @Transactional
    public Booking createWalkInCheckIn(Booking walkInData) {
        // 1. Gọi sang room-service để kiểm tra xem phòng có tồn tại và đang trống (AVAILABLE) không
        RoomDto room = roomClient.getRoomById(walkInData.getRoomId());
        if (!"AVAILABLE".equalsIgnoreCase(room.getStatus())) {
            throw new RuntimeException("Phòng số " + room.getRoomNumber() + " hiện tại không trống để check-in!");
        }

        // 2. Thiết lập thông tin đơn phòng ở luôn
        walkInData.setCheckInDate(LocalDate.now()); // Ngày vào ở là hôm nay luôn
        walkInData.setStatus(BookingStatus.CHECKED_IN); // Trạng thái thẳng lên CHECKED_IN

        // 3. Lưu đơn xuống DB của booking-service
        Booking savedBooking = bookingRepository.save(walkInData);

        // 4. Gọi FeignClient ép room-service đổi trạng thái phòng sang 'OCCUPIED'
        roomClient.updateRoomStatus(walkInData.getRoomId(), new RoomStatusUpdateRequest("OCCUPIED"));

        return savedBooking;
    }

    @Transactional
    public Booking processCheckOut(UUID bookingId) {
        // 1. Kiểm tra đơn đặt phòng có tồn tại không
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đặt phòng với ID: " + bookingId));

        // 2. Kiểm tra xem đơn hàng có đang ở trạng thái CHECKED_IN không thì mới cho check-out
        if (booking.getStatus() != BookingStatus.CHECKED_IN) {
            throw new RuntimeException("Đơn đặt phòng này không ở trạng thái hợp lệ để Check-out!");
        }

        // 3. Cập nhật ngày Check-out thực tế là ngày hôm nay
        booking.setCheckOutDate(LocalDate.now());
        booking.setStatus(BookingStatus.CHECKED_OUT);

        // 4. Tính toán số ngày ở thực tế (Phục vụ cho việc tính tiền sau này)
        long daysBetween = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
        if (daysBetween == 0) {
            daysBetween = 1; // Nếu khách check-in và check-out cùng ngày, tính tròn 1 ngày
        }

        // TODO: Gửi thông tin (daysBetween, roomId) sang billing-service để xuất hóa đơn tại đây nếu cần
        System.out.println("Khách ở tổng cộng: " + daysBetween + " ngày.");

        // 5. Lưu trạng thái đơn đặt phòng xuống DB local
        Booking updatedBooking = bookingRepository.save(booking);

        // 6. Gọi FeignClient ép room-service chuyển trạng thái phòng sang 'DIRTY' để nhân viên đi dọn
        roomClient.updateRoomStatus(booking.getRoomId(), new RoomStatusUpdateRequest("DIRTY"));

        return updatedBooking;
    }
}