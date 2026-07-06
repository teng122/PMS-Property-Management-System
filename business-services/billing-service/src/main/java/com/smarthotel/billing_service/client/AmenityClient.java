package com.smarthotel.billing_service.client;

import com.smarthotel.billing_service.dto.UnpaidAmenityDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

/**
 * Feign client goi S3 amenities-service.
 * Khop chinh xac voi contract amenities cung cap (AmenitiesFeignClient + AmenityOrderController):
 *  - GET  /api/amenities/room/{roomId}/unpaid
 *  - PUT  /api/amenities/orders/{id}/status?status=BILLED  (danh dau tung order da tinh tien)
 */
@FeignClient(name = "amenities-service", path = "/api/amenities")
public interface AmenityClient {

    @GetMapping("/room/{roomId}/unpaid")
    List<UnpaidAmenityDTO> getUnpaid(@PathVariable("roomId") UUID roomId);

    @GetMapping("/orders/booking/{bookingId}/unpaid-charge")
    java.math.BigDecimal getUnpaidCharge(@PathVariable("bookingId") UUID bookingId);

    @PutMapping("/orders/{id}/status")
    UnpaidAmenityDTO updateOrderStatus(@PathVariable("id") UUID id,
                                       @RequestParam("status") String status);
}


