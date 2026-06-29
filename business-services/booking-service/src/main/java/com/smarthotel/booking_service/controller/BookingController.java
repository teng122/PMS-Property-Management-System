package com.smarthotel.booking_service.controller;

import com.smarthotel.booking_service.dto.request.BookingRequest;
import com.smarthotel.booking_service.dto.response.BookingResponse;
import com.smarthotel.booking_service.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/reserve")
    public ResponseEntity<BookingResponse> reserveRoom(@RequestBody BookingRequest request) {
        BookingResponse response = bookingService.createReserve(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}