package com.smarthotel.housekeeping_service.service;

import com.smarthotel.common_shared.event.RoomCleanedEvent;
import com.smarthotel.housekeeping_service.client.RoomServiceClient;
import com.smarthotel.housekeeping_service.dto.request.RoomStatusUpdateRequest;
import com.smarthotel.housekeeping_service.dto.response.CleaningTaskResponse;
import com.smarthotel.housekeeping_service.dto.response.DirtyRoomResponse;
import com.smarthotel.housekeeping_service.entity.CleaningTask;
import com.smarthotel.housekeeping_service.entity.CleaningTaskStatus;
import com.smarthotel.common_shared.model.RoomStatus;
import com.smarthotel.housekeeping_service.exception.CleaningTaskNotFoundException;
import com.smarthotel.housekeeping_service.exception.InvalidCleaningTaskStateException;
import com.smarthotel.housekeeping_service.messaging.producer.HousekeepingEventProducer;
import com.smarthotel.housekeeping_service.repository.CleaningTaskRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service xử lý các nghiệp vụ buồng phòng, dọn phòng của khách sạn.
 * Các phương thức được sắp xếp theo đúng trình tự luồng làm việc thực tế.
 */
@Service
@RequiredArgsConstructor
public class CleaningTaskService {

    private final CleaningTaskRepository cleaningTaskRepository;
    private final ModelMapper modelMapper;
    private final RoomServiceClient roomServiceClient;
    private final HousekeepingEventProducer housekeepingEventProducer;

    // ==========================================
    // 1. TRA CỨU PHÒNG CẦN DỌN (QUERIES)
    // ==========================================

    /**
     * Lấy danh sách các phòng đang bẩn (trạng thái dọn dẹp là PENDING).
     */
    public List<DirtyRoomResponse> getDirtyRooms() {
        return cleaningTaskRepository.findByStatus(CleaningTaskStatus.PENDING)
                .stream()
                .map(task -> modelMapper.map(task, DirtyRoomResponse.class))
                .toList();
    }

    // ==========================================
    // 2. VÒNG ĐỜI CÔNG VIỆC DỌN DẸP (TASK OPERATIONS)
    // ==========================================

    /**
     * Đánh dấu bắt đầu dọn dẹp một phòng cụ thể.
     * Cập nhật trạng thái phòng buồng sang IN_PROGRESS và đồng bộ trạng thái phòng vật lý sang CLEANING.
     */
    public CleaningTaskResponse startTask(UUID taskId) {
        CleaningTask task = cleaningTaskRepository.findByIdOrThrow(taskId);

        if (task.getStatus() != CleaningTaskStatus.PENDING) {
            throw new InvalidCleaningTaskStateException("Task has already started or completed.");
        }

        task.setStatus(CleaningTaskStatus.IN_PROGRESS);

        RoomStatusUpdateRequest request = new RoomStatusUpdateRequest();
        request.setStatus(RoomStatus.CLEANING.name());

        // Gọi đồng bộ trạng thái phòng vật lý qua Feign Client
        roomServiceClient.updateRoomStatus(
                task.getRoomId(),
                request
        );

        CleaningTask updatedTask = cleaningTaskRepository.save(task);

        return modelMapper.map(updatedTask, CleaningTaskResponse.class);
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

        RoomStatusUpdateRequest request = new RoomStatusUpdateRequest();
        request.setStatus(RoomStatus.AVAILABLE.name());

        // Gọi đồng bộ trạng thái phòng vật lý qua Feign Client (Yêu cầu Feign 4)
        roomServiceClient.updateRoomStatus(
                task.getRoomId(),
                request
        );

        CleaningTask updatedTask = cleaningTaskRepository.save(task);

        // Bắn event RoomCleanedEvent lên Kafka để Room Service cập nhật trạng thái phòng sang AVAILABLE (Yêu cầu SAGA 1)
        RoomCleanedEvent event = RoomCleanedEvent.builder()
                .eventId(UUID.randomUUID())
                .roomId(updatedTask.getRoomId())
                .timestamp(LocalDateTime.now())
                .build();
        housekeepingEventProducer.publishRoomCleaned(event);

        return modelMapper.map(updatedTask, CleaningTaskResponse.class);
    }
}