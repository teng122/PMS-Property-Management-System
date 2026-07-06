package com.smarthotel.amenities_service.controller;

import com.smarthotel.amenities_service.dto.request.AmenityCreateRequest;
import com.smarthotel.amenities_service.dto.response.AmenityResponse;
import com.smarthotel.amenities_service.service.AmenityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;
import java.util.UUID;

/**
 * Controller quản lý danh mục dịch vụ tiện ích khách sạn (Amenities Catalog).
 * Sắp xếp các API theo trình tự: Thêm dịch vụ -> Liệt kê danh mục -> Lấy chi tiết dịch vụ.
 */
@RestController
@RequestMapping("/api/amenities")
public class AmenityController {

    @Autowired
    private AmenityService amenityService;

    // ==========================================
    // 1. QUẢN LÝ DANH MỤC TIỆN ÍCH (CATALOG OPERATIONS)
    // ==========================================

    /**
     * Thêm một loại dịch vụ tiện ích mới vào danh mục phục vụ (ví dụ: giặt là, ăn sáng).
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<AmenityResponse> createAmenity(@RequestBody AmenityCreateRequest request) {
        return ResponseEntity.ok(amenityService.createAmenity(request));
    }

    // ==========================================
    // 2. TRUY VẤN DANH MỤC TIỆN ÍCH (QUERIES)
    // ==========================================

    /**
     * Lấy toàn bộ danh sách các dịch vụ tiện ích hiện có trong danh mục.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'RECEPTIONIST', 'ADMIN')")
    public ResponseEntity<List<AmenityResponse>> getAllAmenities() {
        return ResponseEntity.ok(amenityService.getAllAmenities());
    }

    /**
     * Truy vấn thông tin chi tiết của một dịch vụ tiện ích bằng ID dịch vụ.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'RECEPTIONIST', 'ADMIN')")
    public ResponseEntity<AmenityResponse> getAmenityById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(amenityService.getAmenityById(id));
    }
}

