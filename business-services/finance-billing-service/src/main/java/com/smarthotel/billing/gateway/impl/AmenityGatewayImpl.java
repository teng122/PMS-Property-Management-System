package com.smarthotel.billing.gateway.impl;

import com.smarthotel.billing.client.AmenityClient;
import com.smarthotel.billing.dto.UnpaidAmenityDTO;
import com.smarthotel.billing.gateway.AmenityGateway;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Profile("!mock")
public class AmenityGatewayImpl implements AmenityGateway {

    private final AmenityClient client;

    public AmenityGatewayImpl(AmenityClient client) {
        this.client = client;
    }

    @Override
    public List<UnpaidAmenityDTO> getUnpaid(UUID roomId) {
        return client.getUnpaid(roomId);
    }

    @Override
    public void markBilled(UUID roomId) {
        client.markBilled(roomId);
    }
}
