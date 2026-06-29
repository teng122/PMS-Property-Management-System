package com.smarthotel.amenities.service;

import com.smarthotel.amenities.dto.request.AmenityCreateRequest;
import com.smarthotel.amenities.dto.response.AmenityResponse;
import java.util.List;
import java.util.UUID;

public interface AmenityService {
    AmenityResponse createAmenity(AmenityCreateRequest request);
    List<AmenityResponse> getAllAmenities();
    AmenityResponse getAmenityById(UUID id);
}
