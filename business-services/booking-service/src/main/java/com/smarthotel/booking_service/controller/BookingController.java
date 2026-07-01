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

    // tham số đầu vào để chạy {"roomId": "a3b8c2d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d","customerName": "Nguyen Van A"}
    @PostMapping("/check-in")
    public ResponseEntity<Booking> walkInCheckIn(@RequestBody Booking walkInData) {
        Booking newBooking = bookingService.createWalkInCheckIn(walkInData);
        return new ResponseEntity<>(newBooking, HttpStatus.CREATED);
    }

    // tham số đầu vào để chạy: {id} query param nằm ở url
    @PostMapping("/{id}/check-out")
    public ResponseEntity<Booking> checkOut(@PathVariable("id") UUID id) {
        Booking checkedOutBooking = bookingService.processCheckOut(id);
        return ResponseEntity.ok(checkedOutBooking);
    }
}