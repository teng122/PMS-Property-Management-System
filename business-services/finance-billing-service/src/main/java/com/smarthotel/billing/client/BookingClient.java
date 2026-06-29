package com.smarthotel.billing.client;

import com.smarthotel.billing.dto.BookingBillingDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@Profile("!mock")
@FeignClient(name = "room-booking-service")
public interface BookingClient {

    @GetMapping("/api/bookings/{id}/billing-info")
    BookingBillingDTO getBillingInfo(@PathVariable("id") UUID bookingId);
}
