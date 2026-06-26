package com.smarthotel.amenities.controller;

import com.smarthotel.amenities.dto.request.AmenityOrderCreateRequest;
import com.smarthotel.amenities.dto.response.AmenityOrderResponse;
import com.smarthotel.amenities.service.AmenityOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/amenities")
public class AmenityOrderController {

    @Autowired
    private AmenityOrderService amenityOrderService;

    @PostMapping("/order")
    public ResponseEntity<AmenityOrderResponse> createOrder(@RequestBody AmenityOrderCreateRequest request) {
        return ResponseEntity.ok(amenityOrderService.createOrder(request));
    }

    @GetMapping("/room/{roomId}/unpaid")
    public ResponseEntity<List<AmenityOrderResponse>> getUnpaidByRoomId(@PathVariable("roomId") UUID roomId) {
        return ResponseEntity.ok(amenityOrderService.getUnpaidByRoomId(roomId));
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<AmenityOrderResponse> updateOrderStatus(
            @PathVariable("id") UUID id,
            @RequestParam("status") String status) {
        return ResponseEntity.ok(amenityOrderService.updateOrderStatus(id, status));
    }
}
