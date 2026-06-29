package com.smarthotel.billing.gateway.mock;

import com.smarthotel.billing.dto.UnpaidAmenityDTO;
import com.smarthotel.billing.gateway.AmenityGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
@Profile("mock")
public class AmenityGatewayMock implements AmenityGateway {

    private static final Logger log = LoggerFactory.getLogger(AmenityGatewayMock.class);

    @Override
    public List<UnpaidAmenityDTO> getUnpaid(UUID roomId) {
        // serviceCharge = totalPrice (30.000) + totalPrice (45.000) = 75.000
        return List.of(
                new UnpaidAmenityDTO(UUID.randomUUID(), roomId, UUID.randomUUID(),
                        "Coca", new BigDecimal("15000"), 2, new BigDecimal("30000"), "PENDING"),
                new UnpaidAmenityDTO(UUID.randomUUID(), roomId, UUID.randomUUID(),
                        "Mi ly", new BigDecimal("45000"), 1, new BigDecimal("45000"), "DELIVERED")
        );
    }

    @Override
    public void markBilled(List<UUID> orderIds) {
        log.info("[MOCK] markBilled orders={} -> no-op", orderIds);
    }
}
