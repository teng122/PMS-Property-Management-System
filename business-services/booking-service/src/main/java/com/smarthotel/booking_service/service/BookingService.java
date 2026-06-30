package com.smarthotel.booking_service.service;

import com.smarthotel.booking_service.dto.request.BookingRequest;
import com.smarthotel.booking_service.dto.response.BookingResponse;
import com.smarthotel.booking_service.entity.Booking;
import com.smarthotel.booking_service.entity.BookingStatus;
import com.smarthotel.booking_service.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;

    @Transactional
    public BookingResponse createReserve(BookingRequest request) {
        // 1. Tạo thực thể Booking mới với trạng thái PENDING_PAYMENT
        Booking booking = Booking.builder()
                .roomId(request.getRoomId())
                .customerName(request.getCustomerName())
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .status(BookingStatus.PENDING_PAYMENT)
                .build();

        // 2. Lưu vào database
        Booking savedBooking = bookingRepository.save(booking);

        // 3. Map sang dữ liệu trả về cho client
        return mapToResponse(savedBooking);
    }

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
}