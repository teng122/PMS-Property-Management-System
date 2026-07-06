package com.smarthotel.housekeeping_service.controller;

import com.smarthotel.housekeeping_service.dto.response.CleaningTaskResponse;
import com.smarthotel.housekeeping_service.dto.response.DirtyRoomResponse;
import com.smarthotel.housekeeping_service.service.CleaningTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

/**
 * Controller quản lý luồng công việc dọn dẹp buồng phòng (Housekeeping).
 * Sắp xếp quy trình: Xem phòng bẩn -> Bắt đầu dọn phòng -> Hoàn thành dọn phòng.
 */
@RestController
@RequestMapping("/api/housekeeping")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('STAFF', 'ADMIN', 'RECEPTIONIST')")
public class CleaningTaskController {

    private final CleaningTaskService cleaningTaskService;

    // ==========================================
    // 1. TRA CỨU PHÒNG CẦN DỌN (QUERIES)
    // ==========================================

    /**
     * Lấy danh sách toàn bộ các phòng đang ở trạng thái bẩn (DIRTY) chờ được nhân viên dọn dẹp.
     */
    @GetMapping("/dirty-rooms")
    public ResponseEntity<List<DirtyRoomResponse>> getDirtyRooms() {
        return ResponseEntity.ok(cleaningTaskService.getDirtyRooms());
    }

    /**
     * Lấy danh sách công việc dọn phòng, lọc theo trạng thái và/hoặc nhân viên (màn "Việc của tôi").
     */
    @GetMapping("/tasks")
    public ResponseEntity<List<CleaningTaskResponse>> getTasks(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "staffId", required = false) UUID staffId) {
        return ResponseEntity.ok(cleaningTaskService.getTasksByFilters(status, staffId));
    }

    // ==========================================
    // 2. VÒNG ĐỜI CÔNG VIỆC DỌN DẸP (TASK OPERATIONS)
    // ==========================================

    /**
     * Bắt đầu tiến hành dọn phòng.
     * Chuyển trạng thái công việc sang IN_PROGRESS, ghi nhận nhân viên nhận việc (X-User-Id)
     * và cập nhật trạng thái phòng vật lý thành CLEANING.
     */
    @PostMapping("/tasks/{id}/start")
    public ResponseEntity<CleaningTaskResponse> startTask(
            @PathVariable UUID id,
            @RequestHeader(value = "X-User-Id", required = false) UUID staffId) {
        return ResponseEntity.ok(cleaningTaskService.startTask(id, staffId));
    }

    /**
     * Hoàn thành dọn phòng.
     * Chuyển trạng thái công việc sang COMPLETED và cập nhật phòng vật lý thành AVAILABLE.
     */
    @PostMapping("/tasks/{id}/complete")
    public ResponseEntity<CleaningTaskResponse> completeTask(@PathVariable UUID id) {
        return ResponseEntity.ok(cleaningTaskService.completeTask(id));
    }
}