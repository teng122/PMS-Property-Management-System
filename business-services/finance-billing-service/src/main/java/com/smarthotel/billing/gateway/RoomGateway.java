package com.smarthotel.billing.gateway;

import com.smarthotel.billing.dto.RoomInfoDTO;

import java.util.UUID;

public interface RoomGateway {

    /** Lay thong tin phong (chu yeu can price) tu room-service theo roomId. */
    RoomInfoDTO getRoom(UUID roomId);
}
