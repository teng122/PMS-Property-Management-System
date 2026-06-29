package com.smarthotel.booking_service.client;

import com.smarthotel.booking_service.dto.external.RoomDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

// 'name' phải trùng chính xác với 'spring.application.name' của room-service trên Eureka
@FeignClient(name = "room-service") 
public interface RoomClient {

    // Cấu hình đường dẫn API y hệt như bên RoomController của room-service
    @GetMapping("/api/rooms/{id}")
    RoomDto getRoomById(@PathVariable("id") UUID id);
}