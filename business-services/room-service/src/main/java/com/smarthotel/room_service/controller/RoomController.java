package com.smarthotel.room_service.controller;

import com.smarthotel.room_service.dto.request.RoomCreateRequest;
import com.smarthotel.room_service.dto.request.RoomStatusUpdateRequest;
import com.smarthotel.room_service.dto.response.RoomResponse;
import com.smarthotel.room_service.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;
import java.util.UUID;

/**
 * Controller quản lý thông tin phòng vật lý và tìm kiếm phòng trống.
 * Các API được sắp xếp hợp lý theo mục đích: Tìm kiếm -> Cập nhật trạng thái -> Tra cứu.
 */
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    // ==========================================
    // 1. TÌM KIẾM PHÒNG (ROOM SEARCH)
    // ==========================================

    /**
     * Tìm kiếm danh sách các phòng trống trong khoảng thời gian đặt phòng xác định.
     */
    @GetMapping("/search")
    public ResponseEntity<List<RoomResponse>> searchRooms(
            @RequestParam("checkIn") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate checkIn,
            @RequestParam("checkOut") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate checkOut) {
        return ResponseEntity.ok(roomService.searchAvailableRooms(checkIn, checkOut));
    }

    // ==========================================
    // 2. CẬP NHẬT TRẠNG THÁI PHÒNG (ROOM OPERATIONS)
    // ==========================================

    /**
     * Admin tạo mới một phòng vật lý.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomResponse> createRoom(@RequestBody RoomCreateRequest request) {
        return ResponseEntity.ok(roomService.createRoom(request));
    }

    /**
     * Cập nhật trạng thái vật lý của một phòng cụ thể (ví dụ: OCCUPIED, CLEANING, AVAILABLE).
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
    public ResponseEntity<RoomResponse> updateRoomStatus(
            @PathVariable("id") UUID id,
            @RequestBody RoomStatusUpdateRequest request) {
        return ResponseEntity.ok(roomService.updateRoomStatus(id, request));
    }

    // ==========================================
    // 3. TRA CỨU THÔNG TIN PHÒNG (ROOM QUERIES)
    // ==========================================

    /**
     * Lấy thông tin chi tiết một phòng bằng ID phòng.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    /**
     * Lấy danh sách tất cả các phòng đang trống ở thời điểm hiện tại.
     */
    @GetMapping
    public ResponseEntity<List<RoomResponse>> getAvailableRooms() {
        return ResponseEntity.ok(roomService.getAvailableRooms());
    }
}