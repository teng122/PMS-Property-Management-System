package com.smarthotel.housekeeping_service.dto.request;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.UUID;

@Getter @Setter
public class RoomCreateRequest {
    private String roomNumber;
    private String type;
    private BigDecimal price;
    private String status;
    private UUID hotelId;
}