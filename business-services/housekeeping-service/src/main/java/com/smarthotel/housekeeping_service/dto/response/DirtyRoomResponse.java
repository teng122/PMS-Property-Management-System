package com.smarthotel.housekeeping_service.dto.response;

import com.smarthotel.housekeeping_service.entity.CleaningTaskStatus;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
public class DirtyRoomResponse {

    private UUID id;
    private UUID roomId;
    private UUID staffId;
    private CleaningTaskStatus status;
}
