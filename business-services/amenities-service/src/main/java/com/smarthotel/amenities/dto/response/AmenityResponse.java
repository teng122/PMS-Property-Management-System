package com.smarthotel.amenities.dto.response;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.UUID;

@Getter @Setter
public class AmenityResponse {
    private UUID id;
    private String name;
    private BigDecimal price;
    private String type;
}
