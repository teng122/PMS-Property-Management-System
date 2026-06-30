package com.smarthotel.housekeeping_service.service.impl;

import com.smarthotel.housekeeping_service.client.RoomServiceClient;
import com.smarthotel.housekeeping_service.dto.request.RoomStatusUpdateRequest;
import com.smarthotel.housekeeping_service.dto.response.CleaningTaskResponse;
import com.smarthotel.housekeeping_service.dto.response.DirtyRoomResponse;
import com.smarthotel.housekeeping_service.entity.CleaningTask;
import com.smarthotel.housekeeping_service.entity.CleaningTaskStatus;
import com.smarthotel.housekeeping_service.repository.CleaningTaskRepository;
import com.smarthotel.housekeeping_service.service.CleaningTaskService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CleaningTaskServiceImpl implements CleaningTaskService {

    private final CleaningTaskRepository cleaningTaskRepository;
    private final ModelMapper modelMapper;
    private final RoomServiceClient roomServiceClient;

    @Override
    public List<DirtyRoomResponse> getDirtyRooms() {

        return cleaningTaskRepository.findByStatus(CleaningTaskStatus.ASSIGNED)
                .stream()
                .map(task -> modelMapper.map(task, DirtyRoomResponse.class))
                .toList();
    }

    @Override
    public CleaningTaskResponse startTask(UUID taskId) {

        CleaningTask task = cleaningTaskRepository.findById(taskId)
                .orElseThrow(() ->
                        new RuntimeException("Cleaning task not found."));

        if (task.getStatus() != CleaningTaskStatus.ASSIGNED) {
            throw new IllegalStateException("Task has already started or completed.");
        }

        task.setStatus(CleaningTaskStatus.IN_PROGRESS);

        RoomStatusUpdateRequest request = new RoomStatusUpdateRequest();
        request.setStatus("CLEANING");

        roomServiceClient.updateRoomStatus(
                task.getRoomId(),
                request
        );

        CleaningTask updatedTask = cleaningTaskRepository.save(task);

        return modelMapper.map(updatedTask, CleaningTaskResponse.class);
    }

    @Override
    public CleaningTaskResponse completeTask(UUID taskId) {

        CleaningTask task = cleaningTaskRepository.findById(taskId)
                .orElseThrow(() ->
                        new RuntimeException("Cleaning task not found."));

        if (task.getStatus() != CleaningTaskStatus.IN_PROGRESS) {
            throw new IllegalStateException("Task is not in progress.");
        }

        task.setStatus(CleaningTaskStatus.COMPLETED);


        RoomStatusUpdateRequest request = new RoomStatusUpdateRequest();
        request.setStatus("AVAILABLE");

        roomServiceClient.updateRoomStatus(
                task.getRoomId(),
                request
        );

        CleaningTask updatedTask = cleaningTaskRepository.save(task);

        return modelMapper.map(updatedTask, CleaningTaskResponse.class);
    }
}