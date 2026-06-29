package com.smarthotel.booking_service.service;

import com.smarthotel.booking_service.dto.request.BookingRequest;
import com.smarthotel.booking_service.dto.response.BookingResponse;
import com.smarthotel.booking_service.entity.Booking;
import com.smarthotel.booking_service.entity.BookingStatus;
import com.smarthotel.booking_service.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}