package com.smarthotel.booking_service.dto.external;

import lombok.Data;
import java.util.UUID;

@Data
public class RoomDto {
    private UUID id;
    private String roomNumber;
    private String roomType;
    private String status;
    private Double price;
}