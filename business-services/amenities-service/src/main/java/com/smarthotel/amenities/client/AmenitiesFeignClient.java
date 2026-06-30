package com.smarthotel.amenities.client;

import com.smarthotel.amenities.dto.response.AmenityOrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.UUID;

/**
 * Feign Client interface for other microservices (like S5 Billing Service)
 * to call S3 Amenities Service endpoints.
 */
@FeignClient(name = "amenities-service", path = "/api/amenities")
public interface AmenitiesFeignClient {

    /**
     * Get list of unpaid orders for a specific room.
     * Used by S5 Billing Service during checkout.
     */
    @GetMapping("/room/{roomId}/unpaid")
    List<AmenityOrderResponse> getUnpaidByRoomId(@PathVariable("roomId") UUID roomId);

    /**
     * Update the status of a specific order (e.g., PENDING -> BILLED).
     * Used by S5 Billing Service after payment is finalized.
     */
    @PutMapping("/orders/{id}/status")
    AmenityOrderResponse updateOrderStatus(
            @PathVariable("id") UUID id,
            @RequestParam("status") String status);
}
