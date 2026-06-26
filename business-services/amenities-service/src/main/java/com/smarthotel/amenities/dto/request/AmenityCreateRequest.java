package com.smarthotel.amenities.dto.request;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter @Setter
public class AmenityCreateRequest {
    private String name;
    private BigDecimal price;
    private String type; // FOOD, LAUNDRY, SPA
}
