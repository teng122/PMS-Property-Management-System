package com.smarthotel.room_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "booking-service")
public interface BookingClient {

    @GetMapping("/api/bookings/active-room-ids")
    List<UUID> getActiveRoomIds(
            @RequestParam("checkIn") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam("checkOut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut
    );

    @GetMapping("/api/bookings/check-availability")
    boolean checkAvailability(
            @RequestParam("roomId") UUID roomId,
            @RequestParam("bookingId") UUID bookingId,
            @RequestParam("checkIn") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime checkIn,
            @RequestParam("checkOut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime checkOut
    );
}
