package com.smarthotel.billing.client.fallback;

import com.smarthotel.billing.client.RoomClient;
import com.smarthotel.billing.dto.response.RoomInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Circuit-breaker fallback cho {@link RoomClient}.
 * Kich hoat khi room-service loi/timeout hoac circuit dang OPEN.
 * Tra ve phong "khong xac dinh" gia 0 de billing khong bi treo/sap.
 */
@Slf4j
@Component
public class RoomClientFallback implements RoomClient {

    @Override
    public RoomInfoDTO getRoom(UUID roomId) {
        log.warn("[CircuitBreaker] room-service khong kha dung -> fallback getRoom({})", roomId);
        return new RoomInfoDTO(roomId, "N/A", "UNKNOWN", BigDecimal.ZERO, "UNAVAILABLE");
    }
}
