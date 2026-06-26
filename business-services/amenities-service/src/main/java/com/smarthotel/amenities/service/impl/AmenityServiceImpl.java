package com.smarthotel.amenities.service.impl;

import com.smarthotel.amenities.dto.request.AmenityCreateRequest;
import com.smarthotel.amenities.dto.response.AmenityResponse;
import com.smarthotel.amenities.entity.Amenity;
import com.smarthotel.amenities.exception.ResourceNotFoundException;
import com.smarthotel.amenities.repository.AmenityRepository;
import com.smarthotel.amenities.service.AmenityService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AmenityServiceImpl implements AmenityService {

    @Autowired
    private AmenityRepository amenityRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public AmenityResponse createAmenity(AmenityCreateRequest request) {
        Amenity amenity = new Amenity();
        amenity.setName(request.getName());
        amenity.setPrice(request.getPrice());
        amenity.setType(request.getType().toUpperCase());

        Amenity saved = amenityRepository.save(amenity);
        return modelMapper.map(saved, AmenityResponse.class);
    }

    @Override
    public List<AmenityResponse> getAllAmenities() {
        return amenityRepository.findAll().stream()
                .map(amenity -> modelMapper.map(amenity, AmenityResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public AmenityResponse getAmenityById(UUID id) {
        Amenity amenity = amenityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ với ID: " + id));
        return modelMapper.map(amenity, AmenityResponse.class);
    }
}
