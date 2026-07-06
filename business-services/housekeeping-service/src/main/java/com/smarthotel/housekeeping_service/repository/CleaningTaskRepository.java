package com.smarthotel.housekeeping_service.repository;

import com.smarthotel.housekeeping_service.entity.CleaningTask;
import com.smarthotel.housekeeping_service.entity.CleaningTaskStatus;
import com.smarthotel.housekeeping_service.exception.CleaningTaskNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CleaningTaskRepository extends JpaRepository<CleaningTask, UUID> {

    default CleaningTask findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new CleaningTaskNotFoundException("Cleaning task not found with ID: " + id));
    }

    List<CleaningTask> findByStatus(CleaningTaskStatus status);

    List<CleaningTask> findByStaffId(UUID staffId);

    List<CleaningTask> findByRoomId(UUID roomId);

    List<CleaningTask> findByStaffIdAndStatus(UUID staffId,
                                              CleaningTaskStatus status);
}