package com.smarthotel.amenities_service.service;

import com.smarthotel.amenities_service.dto.request.AmenityCreateRequest;
import com.smarthotel.amenities_service.dto.response.AmenityResponse;
import com.smarthotel.amenities_service.entity.Amenity;
import com.smarthotel.amenities_service.exception.ResourceNotFoundException;
import com.smarthotel.amenities_service.repository.AmenityRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.smarthotel.amenities_service.entity.AmenityStatus;

/**
 * Service quản lý danh mục dịch vụ tiện ích phục vụ trong khách sạn.
 * Các hàm được sắp xếp theo đúng trình tự nghiệp vụ của danh mục tiện ích.
 */
@Service
public class AmenityService {

    @Autowired
    private AmenityRepository amenityRepository;

    @Autowired
    private ModelMapper modelMapper;

    // ==========================================
    // 1. QUẢN LÝ DANH MỤC TIỆN ÍCH (CATALOG OPERATIONS)
    // ==========================================

    /**
     * Thêm mới một loại tiện ích dịch vụ vào hệ thống (ví dụ: Massage, Ăn sáng, Giặt là...).
     */
    public AmenityResponse createAmenity(AmenityCreateRequest request) {
        Amenity amenity = new Amenity();
        amenity.setName(request.getName());
        amenity.setPrice(request.getPrice());
        amenity.setType(request.getType().toUpperCase());
        amenity.setStatus(AmenityStatus.AVAILABLE);
        amenity.setIsReturnable(request.getIsReturnable() != null ? request.getIsReturnable() : false);

        Amenity saved = amenityRepository.save(amenity);
        return modelMapper.map(saved, AmenityResponse.class);
    }

    // ==========================================
    // 2. TRUY VẤN DANH MỤC TIỆN ÍCH (QUERIES)
    // ==========================================

    /**
     * Lấy toàn bộ danh sách tiện ích dịch vụ hiện có trong danh mục.
     */
    public List<AmenityResponse> getAllAmenities() {
        return amenityRepository.findAll().stream()
                .map(amenity -> modelMapper.map(amenity, AmenityResponse.class))
                .collect(Collectors.toList());
    }

    /**
     * Tìm thông tin tiện ích dịch vụ chi tiết dựa trên ID.
     */
    public AmenityResponse getAmenityById(UUID id) {
        Amenity amenity = amenityRepository.findByIdOrThrow(id);
        return modelMapper.map(amenity, AmenityResponse.class);
    }
}

