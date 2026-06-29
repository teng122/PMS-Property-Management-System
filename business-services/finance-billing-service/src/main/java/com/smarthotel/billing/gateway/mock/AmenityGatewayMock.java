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
        // serviceCharge = 30.000 + 45.000 = 75.000
        return List.of(
                new UnpaidAmenityDTO(UUID.randomUUID(), "Coca", 2, new BigDecimal("30000")),
                new UnpaidAmenityDTO(UUID.randomUUID(), "Mi ly", 1, new BigDecimal("45000"))
        );
    }

    @Override
    public void markBilled(UUID roomId) {
        log.info("[MOCK] markBilled room={} -> no-op", roomId);
    }
}
