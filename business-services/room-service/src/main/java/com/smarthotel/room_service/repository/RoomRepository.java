package com.smarthotel.room_service.repository;

import com.smarthotel.room_service.entity.Room;
import com.smarthotel.common_shared.model.RoomStatus;
import com.smarthotel.room_service.exception.RoomNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {
    List<Room> findByStatus(RoomStatus status);

    default Room findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new RoomNotFoundException("Không tìm thấy phòng với ID: " + id));
    }
}