package com.smarthotel.housekeeping_service.client;

import com.smarthotel.housekeeping_service.dto.request.RoomStatusUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "room-service", fallback = com.smarthotel.housekeeping_service.client.fallback.RoomServiceClientFallback.class)
public interface RoomServiceClient {

    @PutMapping("/api/rooms/{id}/status")
    void updateRoomStatus(
            @PathVariable("id") UUID id,
            @RequestBody RoomStatusUpdateRequest request
    );

}