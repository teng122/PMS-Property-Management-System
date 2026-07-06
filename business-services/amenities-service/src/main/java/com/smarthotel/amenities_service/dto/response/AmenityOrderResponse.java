package com.smarthotel.amenities_service.dto.response;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.UUID;

@Getter @Setter
public class AmenityOrderResponse {
    private UUID id;
    private UUID roomId;
    private UUID bookingId;
    private String amenityName;
    private BigDecimal amenityPrice;
    private Integer quantity;
    private BigDecimal totalPrice;
    private String status;
}

