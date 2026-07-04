package com.smarthotel.billing.client;

import com.smarthotel.billing.dto.response.RoomInfoDTO;
import com.smarthotel.billing.client.fallback.RoomClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * Feign client goi room-service de lay gia phong.
 *
 * <p><b>PHU THUOC CON THIEU O room-service:</b> hien tai room-service chi co
 * {@code GET /api/rooms/search} (chi tra phong AVAILABLE) va {@code PUT /api/rooms/{id}/status}.
 * De S5 tinh tien phong that (profile docker), room-service can bo sung getter theo id:
 * <pre>GET /api/rooms/{id} -> RoomResponse (da co san field price)</pre>
 * Truoc do, S5 van chay doc lap o profile {@code mock}.
 */
@FeignClient(name = "room-service", fallback = RoomClientFallback.class)
public interface RoomClient {

    @GetMapping("/api/rooms/{id}")
    RoomInfoDTO getRoom(@PathVariable("id") UUID roomId);
}
