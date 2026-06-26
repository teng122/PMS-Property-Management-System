package com.smarthotel.amenities.controller;

import com.smarthotel.amenities.dto.request.AmenityCreateRequest;
import com.smarthotel.amenities.dto.response.AmenityResponse;
import com.smarthotel.amenities.service.AmenityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/amenities")
public class AmenityController {

    @Autowired
    private AmenityService amenityService;

    @PostMapping
    public ResponseEntity<AmenityResponse> createAmenity(@RequestBody AmenityCreateRequest request) {
        return ResponseEntity.ok(amenityService.createAmenity(request));
    }

    @GetMapping
    public ResponseEntity<List<AmenityResponse>> getAllAmenities() {
        return ResponseEntity.ok(amenityService.getAllAmenities());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AmenityResponse> getAmenityById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(amenityService.getAmenityById(id));
    }
}
