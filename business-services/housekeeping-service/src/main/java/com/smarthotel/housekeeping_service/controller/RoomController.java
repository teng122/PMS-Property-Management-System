package com.smarthotel.housekeeping_service.controller;

import com.smarthotel.housekeeping_service.dto.request.RoomStatusUpdateRequest;
import com.smarthotel.housekeeping_service.dto.response.RoomResponse;
import com.smarthotel.housekeeping_service.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @GetMapping("/search")
    public ResponseEntity<List<RoomResponse>> searchRooms() {
        return ResponseEntity.ok(roomService.getAvailableRooms());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateRoomStatus(
            @PathVariable("id") UUID id,
            @RequestBody RoomStatusUpdateRequest request) {
        try {
            return ResponseEntity.ok(roomService.updateRoomStatus(id, request));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("LỖI THẬT SỰ LÀ: " + e.getMessage() + " | Tại: " + e.getClass().getName());
        }
    }
}