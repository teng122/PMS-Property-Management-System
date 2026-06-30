package com.smarthotel.housekeeping_service.controller;

import com.smarthotel.housekeeping_service.dto.response.CleaningTaskResponse;
import com.smarthotel.housekeeping_service.dto.response.DirtyRoomResponse;
import com.smarthotel.housekeeping_service.service.CleaningTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/housekeeping")
@RequiredArgsConstructor
public class CleaningTaskController {

    private final CleaningTaskService cleaningTaskService;

    /**
     * GET /api/housekeeping/dirty-rooms
     * Returns all rooms waiting to be cleaned.
     */
    @GetMapping("/dirty-rooms")
    public ResponseEntity<List<DirtyRoomResponse>> getDirtyRooms() {
        return ResponseEntity.ok(cleaningTaskService.getDirtyRooms());
    }

    /**
     * POST /api/housekeeping/tasks/{id}/start
     * Marks a cleaning task as IN_PROGRESS.
     */
    @PostMapping("/tasks/{id}/start")
    public ResponseEntity<CleaningTaskResponse> startTask(
            @PathVariable UUID id) {

        return ResponseEntity.ok(cleaningTaskService.startTask(id));
    }

    /**
     * POST /api/housekeeping/tasks/{id}/complete
     * Marks a cleaning task as COMPLETED.
     */
    @PostMapping("/tasks/{id}/complete")
    public ResponseEntity<CleaningTaskResponse> completeTask(
            @PathVariable UUID id) {

        return ResponseEntity.ok(cleaningTaskService.completeTask(id));
    }
}