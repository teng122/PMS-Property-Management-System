package com.smarthotel.amenities.repository;

import com.smarthotel.amenities.entity.AmenityOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AmenityOrderRepository extends JpaRepository<AmenityOrder, UUID> {
    List<AmenityOrder> findByRoomIdAndStatusIn(UUID roomId, List<String> statuses);
}
