package com.smarthotel.billing.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Contract nhan tu room-service: GET /api/rooms/{id}.
 * Khop voi com.smarthotel.room_service.dto.response.RoomResponse.
 * S5 chi can {@code price} (gia mot dem) de tinh tien phong.
 */
public record RoomInfoDTO(
        UUID id,
        String roomNumber,
        String type,
        BigDecimal price,
        String status) {}
