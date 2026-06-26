package com.smarthotel.housekeeping_service.repository;

import com.smarthotel.housekeeping_service.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {
    List<Room> findByStatus(String status);
}