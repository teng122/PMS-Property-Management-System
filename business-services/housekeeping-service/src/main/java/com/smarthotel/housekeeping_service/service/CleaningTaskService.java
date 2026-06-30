package com.smarthotel.housekeeping_service.service;

import com.smarthotel.housekeeping_service.dto.response.CleaningTaskResponse;
import com.smarthotel.housekeeping_service.dto.response.DirtyRoomResponse;

import java.util.List;
import java.util.UUID;

public interface CleaningTaskService {

    List<DirtyRoomResponse> getDirtyRooms();

    CleaningTaskResponse startTask(UUID taskId);

    CleaningTaskResponse completeTask(UUID taskId);
}