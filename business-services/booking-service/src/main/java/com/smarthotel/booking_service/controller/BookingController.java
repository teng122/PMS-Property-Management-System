package com.smarthotel.booking_service.controller;

import com.smarthotel.booking_service.dto.request.BookingRequest;
import com.smarthotel.booking_service.dto.response.BookingResponse;
import com.smarthotel.booking_service.entity.Booking;
import com.smarthotel.booking_service.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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

    // 1. GET ALL: GET http://localhost:8082/api/bookings
    @GetMapping
    public ResponseEntity<List<Booking>> getAll() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    // 2. FIND BY ID: GET http://localhost:8082/api/bookings/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Booking> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    // 3. UPDATE: PUT http://localhost:8082/api/bookings/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Booking> update(@PathVariable UUID id, @RequestBody Booking updatedData) {
        return ResponseEntity.ok(bookingService.updateBooking(id, updatedData));
    }

    // 4. DELETE: DELETE http://localhost:8082/api/bookings/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable UUID id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.ok("Xóa thành công đơn đặt phòng có ID: " + id);
    }
}