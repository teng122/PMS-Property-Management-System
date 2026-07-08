package com.smarthotel.housekeeping_service.client.fallback;

import com.smarthotel.housekeeping_service.client.RoomServiceClient;
import com.smarthotel.housekeeping_service.dto.request.RoomStatusUpdateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
@Slf4j
public class RoomServiceClientFallback implements RoomServiceClient {

    @Override
    public void updateRoomStatus(UUID id, RoomStatusUpdateRequest request) {
        log.error("[Fallback] Room Service is offline or timed out. Cannot update room {} status to {}", id, request.getStatus());
    }
}
