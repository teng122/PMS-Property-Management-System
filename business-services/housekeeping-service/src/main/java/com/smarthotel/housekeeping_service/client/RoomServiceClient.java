package com.smarthotel.housekeeping_service.client;

import com.smarthotel.housekeeping_service.dto.external.RoomDto;
import com.smarthotel.housekeeping_service.dto.request.RoomStatusUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "room-service", fallback = com.smarthotel.housekeeping_service.client.fallback.RoomServiceClientFallback.class)
public interface RoomServiceClient {

    @GetMapping("/api/rooms/{id}")
    RoomDto getRoomById(@PathVariable("id") UUID id);

    // Dùng endpoint nội bộ (permitAll) để STAFF bắt đầu dọn phòng không bị 403.
    @PutMapping("/api/rooms/internal/{id}/status")
    void updateRoomStatus(
            @PathVariable("id") UUID id,
            @RequestBody RoomStatusUpdateRequest request
    );
}