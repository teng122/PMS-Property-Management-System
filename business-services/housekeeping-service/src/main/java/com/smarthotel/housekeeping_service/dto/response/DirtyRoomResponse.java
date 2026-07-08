package com.smarthotel.housekeeping_service.dto.response;

import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
public class DirtyRoomResponse {
    private UUID id;
    private UUID roomId;
    private String roomNumber;
    private UUID staffId;
}
