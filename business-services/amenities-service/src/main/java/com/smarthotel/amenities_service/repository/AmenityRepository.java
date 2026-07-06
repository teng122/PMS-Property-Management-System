package com.smarthotel.amenities_service.repository;

import com.smarthotel.amenities_service.entity.Amenity;
import com.smarthotel.amenities_service.exception.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AmenityRepository extends JpaRepository<Amenity, UUID> {

    default Amenity findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ với ID: " + id));
    }
}

