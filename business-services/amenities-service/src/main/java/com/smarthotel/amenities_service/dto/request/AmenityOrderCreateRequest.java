package com.smarthotel.amenities_service.dto.request;

import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter @Setter
public class AmenityOrderCreateRequest {
    private UUID roomId;
    private UUID bookingId;
    private UUID amenityId;
    private Integer quantity;
}

