package com.smarthotel.booking_service.client;

import com.smarthotel.booking_service.dto.external.RoomDto;
import com.smarthotel.booking_service.dto.external.RoomStatusUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import com.smarthotel.booking_service.client.fallback.RoomClientFallback;
import java.util.List;

// 'name' phải trùng chính xác với 'spring.application.name' của room-service trên Eureka
@FeignClient(name = "room-service", fallback = RoomClientFallback.class) 
public interface RoomClient {

    // Cấu hình đường dẫn API y hệt như bên RoomController của room-service
    @GetMapping("/api/rooms/{id}")
    RoomDto getRoomById(@PathVariable("id") UUID id);

    @PutMapping("/api/rooms/{id}/status")
    void updateRoomStatus(@PathVariable("id") UUID id, @RequestBody RoomStatusUpdateRequest request);

    // Dùng endpoint nội bộ (permitAll) để luồng tìm phòng public/khách hàng không bị 403.
    @GetMapping("/api/rooms/internal/all")
    List<RoomDto> getAllRooms();
}