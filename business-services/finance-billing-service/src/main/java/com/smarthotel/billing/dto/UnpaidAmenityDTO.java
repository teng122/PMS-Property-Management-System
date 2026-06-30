package com.smarthotel.billing.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Contract nhan tu S3 (amenities-service).
 * Khop voi com.smarthotel.amenities.dto.response.AmenityOrderResponse:
 * GET /api/amenities/room/{roomId}/unpaid -> List<AmenityOrderResponse>.
 * serviceCharge cua hoa don = tong cac {@code totalPrice}.
 */
public record UnpaidAmenityDTO(
        UUID id,
        UUID roomId,
        UUID bookingId,
        String amenityName,
        BigDecimal amenityPrice,
        Integer quantity,
        BigDecimal totalPrice,
        String status) {}
