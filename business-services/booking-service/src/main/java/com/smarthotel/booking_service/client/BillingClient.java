package com.smarthotel.booking_service.client;

import com.smarthotel.booking_service.dto.external.InvoiceDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

@FeignClient(name = "billing-service")
public interface BillingClient {

    @GetMapping("/api/invoices/booking/{bookingId}")
    InvoiceDto getInvoiceByBookingId(@PathVariable("bookingId") UUID bookingId);
}
