package com.smarthotel.billing.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request tao hoa don.
 * Phuong an B: chi can bookingId. S5 tu lay roomId + ngay o tu S2 (GET /api/bookings/{id})
 * va gia phong tu room-service (GET /api/rooms/{id}) roi tinh roomCharge = price x so dem.
 */
public record GenerateRequest(@NotNull UUID bookingId) {}
