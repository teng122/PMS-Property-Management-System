package com.smarthotel.booking_service.dto.external;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor 
public class RoomStatusUpdateRequest {
    private String status;
}