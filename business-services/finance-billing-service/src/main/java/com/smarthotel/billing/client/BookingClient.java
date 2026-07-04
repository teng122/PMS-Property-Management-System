package com.smarthotel.billing.client;

import com.smarthotel.billing.dto.response.BookingInfoDTO;
import com.smarthotel.billing.client.fallback.BookingClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * Feign client goi S2 booking-service.
 * Khop voi endpoint that: GET /api/bookings/{id} -> Booking (id, roomId, dates, status...).
 * S5 chi can roomId; roomCharge do client truyen (phuong an C).
 */
@FeignClient(name = "booking-service", fallback = BookingClientFallback.class)
public interface BookingClient {

    @GetMapping("/api/bookings/{id}")
    BookingInfoDTO getBooking(@PathVariable("id") UUID bookingId);
}
