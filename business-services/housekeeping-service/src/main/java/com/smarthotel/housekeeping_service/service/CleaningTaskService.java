package com.smarthotel.housekeeping_service.service;

import com.smarthotel.common_shared.event.RoomCleanedEvent;
import com.smarthotel.housekeeping_service.client.RoomServiceClient;
import com.smarthotel.housekeeping_service.dto.external.RoomDto;
import com.smarthotel.housekeeping_service.dto.request.RoomStatusUpdateRequest;
import com.smarthotel.housekeeping_service.dto.response.CleaningTaskResponse;
import com.smarthotel.housekeeping_service.dto.response.DirtyRoomResponse;
import com.smarthotel.housekeeping_service.entity.CleaningTask;
import com.smarthotel.housekeeping_service.entity.CleaningTaskStatus;
import com.smarthotel.common_shared.model.RoomStatus;
import com.smarthotel.housekeeping_service.exception.InvalidCleaningTaskStateException;
import com.smarthotel.housekeeping_service.messaging.producer.HousekeepingEventProducer;
import com.smarthotel.housekeeping_service.repository.CleaningTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service xử lý các nghiệp vụ buồng phòng, dọn phòng của khách sạn.
 * Các phương thức được sắp xếp theo đúng trình tự luồng làm việc thực tế.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CleaningTaskService {

    private final CleaningTaskRepository cleaningTaskRepository;
    private final RoomServiceClient roomServiceClient;
    private final HousekeepingEventProducer housekeepingEventProducer;

    // ==========================================
    // 1. TRA CỨU PHÒNG CẦN DỌN (QUERIES)
    // ==========================================

    /**
     * Lấy danh sách các phòng đang bẩn (trạng thái dọn dẹp là PENDING).
     * Enrich roomNumber theo unique roomId — tránh N+1 call.
     */
    public List<DirtyRoomResponse> getDirtyRooms() {
        List<CleaningTask> tasks = cleaningTaskRepository.findByStatus(CleaningTaskStatus.PENDING);
        Map<UUID, String> roomNumberMap = buildRoomNumberMap(tasks);
        return tasks.stream()
                .map(task -> toDirtyRoomResponse(task, roomNumberMap.get(task.getRoomId())))
                .toList();
    }

    /**
     * Lấy danh sách công việc dọn phòng, lọc theo trạng thái và/hoặc nhân viên (màn "Việc của tôi").
     * Bỏ trống cả hai = lấy toàn bộ task.
     */
    public List<CleaningTaskResponse> getTasksByFilters(String statusStr, UUID staffId) {
        CleaningTaskStatus status = null;
        if (statusStr != null && !statusStr.isBlank()) {
            try {
                status = CleaningTaskStatus.valueOf(statusStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new InvalidCleaningTaskStateException("Trạng thái công việc không hợp lệ: " + statusStr);
            }
        }

        List<CleaningTask> tasks;
        if (status != null && staffId != null) {
            tasks = cleaningTaskRepository.findByStaffIdAndStatus(staffId, status);
        } else if (status != null) {
            tasks = cleaningTaskRepository.findByStatus(status);
        } else if (staffId != null) {
            tasks = cleaningTaskRepository.findByStaffId(staffId);
        } else {
            tasks = cleaningTaskRepository.findAll();
        }

        Map<UUID, String> roomNumberMap = buildRoomNumberMap(tasks);
        return tasks.stream()
                .map(task -> toTaskResponse(task, roomNumberMap.get(task.getRoomId())))
                .toList();
    }

    // ==========================================
    // 2. VÒNG ĐỜI CÔNG VIỆC DỌN DẸP (TASK OPERATIONS)
    // ==========================================

    /**
     * Đánh dấu bắt đầu dọn dẹp một phòng cụ thể.
     * Cập nhật trạng thái phòng buồng sang IN_PROGRESS và đồng bộ trạng thái phòng vật lý sang CLEANING.
     */
    public CleaningTaskResponse startTask(UUID taskId, UUID staffId) {
        CleaningTask task = cleaningTaskRepository.findByIdOrThrow(taskId);

        if (task.getStatus() != CleaningTaskStatus.PENDING) {
            throw new InvalidCleaningTaskStateException("Task has already started or completed.");
        }

        task.setStatus(CleaningTaskStatus.IN_PROGRESS);
        // Ghi nhận nhân viên nhận việc để hỗ trợ màn "Việc của tôi"
        if (staffId != null) {
            task.setStaffId(staffId);
        }
        task.setAssignedAt(LocalDateTime.now());

        RoomStatusUpdateRequest request = new RoomStatusUpdateRequest();
        request.setStatus(RoomStatus.CLEANING.name());

        // Gọi đồng bộ trạng thái phòng vật lý qua Feign Client
        roomServiceClient.updateRoomStatus(task.getRoomId(), request);

        CleaningTask updatedTask = cleaningTaskRepository.save(task);
        return toTaskResponse(updatedTask, resolveRoomNumber(updatedTask.getRoomId()));
    }

    /**
     * Đánh dấu hoàn thành việc dọn dẹp phòng vật lý.
     * Cập nhật trạng thái phòng buồng sang COMPLETED và bắn sự kiện RoomCleanedEvent qua Kafka không đồng bộ.
     */
    public CleaningTaskResponse completeTask(UUID taskId) {
        CleaningTask task = cleaningTaskRepository.findByIdOrThrow(taskId);

        if (task.getStatus() != CleaningTaskStatus.IN_PROGRESS) {
            throw new InvalidCleaningTaskStateException("Task is not in progress.");
        }

        task.setStatus(CleaningTaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());

        CleaningTask updatedTask = cleaningTaskRepository.save(task);

        // Bắn event RoomCleanedEvent lên Kafka để Room Service cập nhật trạng thái phòng sang AVAILABLE
        RoomCleanedEvent event = RoomCleanedEvent.builder()
                .eventId(UUID.randomUUID())
                .roomId(updatedTask.getRoomId())
                .timestamp(LocalDateTime.now())
                .build();
        housekeepingEventProducer.publishRoomCleaned(event);

        return toTaskResponse(updatedTask, resolveRoomNumber(updatedTask.getRoomId()));
    }

    // ==========================================
    // PRIVATE HELPERS
    // ==========================================

    /**
     * Gom unique roomId → gọi Room Service 1 lần mỗi ID → trả Map<roomId, roomNumber>.
     */
    private Map<UUID, String> buildRoomNumberMap(List<CleaningTask> tasks) {
        Map<UUID, String> map = new HashMap<>();
        tasks.stream()
                .map(CleaningTask::getRoomId)
                .distinct()
                .forEach(id -> map.put(id, resolveRoomNumber(id)));
        return map;
    }

    /**
     * Gọi Room Service lấy roomNumber. Trả null nếu Room Service unavailable (fallback).
     */
    private String resolveRoomNumber(UUID roomId) {
        try {
            RoomDto room = roomServiceClient.getRoomById(roomId);
            return room != null ? room.getRoomNumber() : null;
        } catch (Exception e) {
            log.warn("[CleaningTaskService] Không thể lấy roomNumber cho roomId {}: {}", roomId, e.getMessage());
            return null;
        }
    }

    private DirtyRoomResponse toDirtyRoomResponse(CleaningTask task, String roomNumber) {
        DirtyRoomResponse r = new DirtyRoomResponse();
        r.setId(task.getId());
        r.setRoomId(task.getRoomId());
        r.setRoomNumber(roomNumber);
        r.setStaffId(task.getStaffId());
        return r;
    }

    private CleaningTaskResponse toTaskResponse(CleaningTask task, String roomNumber) {
        CleaningTaskResponse r = new CleaningTaskResponse();
        r.setId(task.getId());
        r.setRoomId(task.getRoomId());
        r.setRoomNumber(roomNumber);
        r.setStaffId(task.getStaffId());
        r.setStatus(task.getStatus());
        // updatedAt: ưu tiên completedAt, nếu null dùng assignedAt
        r.setUpdatedAt(task.getCompletedAt() != null ? task.getCompletedAt() : task.getAssignedAt());
        return r;
    }
}