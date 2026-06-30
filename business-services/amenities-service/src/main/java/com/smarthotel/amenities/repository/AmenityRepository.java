package com.smarthotel.amenities.repository;

import com.smarthotel.amenities.entity.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AmenityRepository extends JpaRepository<Amenity, UUID> {
}
