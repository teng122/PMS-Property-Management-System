package com.smarthotel.billing.gateway.impl;

import com.smarthotel.billing.client.RoomClient;
import com.smarthotel.billing.dto.RoomInfoDTO;
import com.smarthotel.billing.gateway.RoomGateway;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("!mock")
public class RoomGatewayImpl implements RoomGateway {

    private final RoomClient client;

    public RoomGatewayImpl(RoomClient client) {
        this.client = client;
    }

    @Override
    public RoomInfoDTO getRoom(UUID roomId) {
        return client.getRoom(roomId);
    }
}
