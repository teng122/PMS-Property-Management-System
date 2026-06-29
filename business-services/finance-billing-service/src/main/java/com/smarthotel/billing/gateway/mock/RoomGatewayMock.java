package com.smarthotel.billing.gateway.mock;

import com.smarthotel.billing.dto.RoomInfoDTO;
import com.smarthotel.billing.gateway.RoomGateway;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@Profile("mock")
public class RoomGatewayMock implements RoomGateway {

    // gia 1 dem = 750.000; BookingGatewayMock o 2 dem -> roomCharge = 1.500.000
    public static final BigDecimal MOCK_PRICE_PER_NIGHT = new BigDecimal("750000");

    @Override
    public RoomInfoDTO getRoom(UUID roomId) {
        return new RoomInfoDTO(roomId, "101", "DOUBLE", MOCK_PRICE_PER_NIGHT, "OCCUPIED");
    }
}
