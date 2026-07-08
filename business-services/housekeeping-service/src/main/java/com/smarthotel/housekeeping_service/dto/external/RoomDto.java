package com.smarthotel.housekeeping_service.dto.external;

import lombok.Data;
import java.util.UUID;

@Data
public class RoomDto {
    private UUID id;
    private String roomNumber;
}
