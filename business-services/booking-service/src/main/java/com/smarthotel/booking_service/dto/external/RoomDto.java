package com.smarthotel.booking_service.dto.external;

import lombok.Data;
import java.util.UUID;
import java.math.BigDecimal;

@Data
public class RoomDto {
    private UUID id;
    private String roomNumber;
    private String type;
    private String status;
    private BigDecimal price;
    private UUID reservedBookingId;
}