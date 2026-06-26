package com.smarthotel.housekeeping_service.dto.response;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.UUID;

@Getter @Setter
public class RoomResponse {
    private UUID id;
    private String roomNumber;
    private String type;
    private BigDecimal price;
    private String status;
}