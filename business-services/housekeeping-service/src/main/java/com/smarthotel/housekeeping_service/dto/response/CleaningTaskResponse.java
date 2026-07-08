package com.smarthotel.housekeeping_service.dto.response;

import com.smarthotel.housekeeping_service.entity.CleaningTaskStatus;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class CleaningTaskResponse {
    private UUID id;
    private UUID roomId;
    private String roomNumber;
    private UUID staffId;
    private CleaningTaskStatus status;
    private LocalDateTime updatedAt;
}
