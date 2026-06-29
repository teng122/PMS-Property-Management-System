package com.smarthotel.billing.client;

import com.smarthotel.billing.dto.UnpaidAmenityDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.UUID;

@Profile("!mock")
@FeignClient(name = "extra-amenities-service")
public interface AmenityClient {

    @GetMapping("/api/amenities/room/{roomId}/unpaid")
    List<UnpaidAmenityDTO> getUnpaid(@PathVariable("roomId") UUID roomId);

    @PostMapping("/api/amenities/room/{roomId}/mark-billed")
    void markBilled(@PathVariable("roomId") UUID roomId);
}
