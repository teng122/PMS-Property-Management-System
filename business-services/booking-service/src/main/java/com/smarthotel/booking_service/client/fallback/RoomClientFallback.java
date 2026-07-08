package com.smarthotel.booking_service.client.fallback;

import com.smarthotel.booking_service.client.RoomClient;
import com.smarthotel.booking_service.dto.external.RoomDto;
import com.smarthotel.booking_service.dto.external.RoomStatusUpdateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class RoomClientFallback implements RoomClient {

    @Override
    public RoomDto getRoomById(UUID id) {
        log.warn("[Fallback] Room Service getRoomById is offline or timed out. Returning null for room ID: {}", id);
        return null;
    }

    @Override
    public void updateRoomStatus(UUID id, RoomStatusUpdateRequest request) {
        log.error("[Fallback] Room Service updateRoomStatus is offline or timed out. Cannot update room {} status to {}", id, request.getStatus());
    }

    @Override
    public List<RoomDto> getAllRooms() {
        log.warn("[Fallback] Room Service getAllRooms is offline or timed out. Returning empty list.");
        return Collections.emptyList();
    }
}
