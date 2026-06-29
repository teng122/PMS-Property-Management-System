package com.smarthotel.billing.gateway;

import com.smarthotel.billing.dto.UnpaidAmenityDTO;

import java.util.List;
import java.util.UUID;

public interface AmenityGateway {
    List<UnpaidAmenityDTO> getUnpaid(UUID roomId);
    void markBilled(UUID roomId);
}
