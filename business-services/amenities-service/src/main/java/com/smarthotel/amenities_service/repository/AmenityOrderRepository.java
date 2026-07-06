package com.smarthotel.amenities_service.repository;

import com.smarthotel.amenities_service.entity.AmenityOrder;
import com.smarthotel.amenities_service.exception.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

import com.smarthotel.amenities_service.entity.AmenityOrderStatus;

public interface AmenityOrderRepository extends JpaRepository<AmenityOrder, UUID> {
    List<AmenityOrder> findByRoomIdAndStatusIn(UUID roomId, List<AmenityOrderStatus> statuses);
    List<AmenityOrder> findByBookingIdAndStatusIn(UUID bookingId, List<AmenityOrderStatus> statuses);

    default AmenityOrder findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn dịch vụ với ID: " + id));
    }
}

