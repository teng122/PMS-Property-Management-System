package com.smarthotel.billing.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Contract nhan tu S2 (booking-service): GET /api/bookings/{id}.
 * Chi lay cac truong S5 can (roomId de tra cuu dich vu S3); cac truong khac
 * cua Booking (status, createdAt) bi bo qua khi deserialize.
 *
 * <p>Luu y: roomCharge (tien phong) KHONG nam o day - theo phuong an C,
 * client truyen roomCharge vao request {@code generate}.
 */
public record BookingInfoDTO(
        UUID id,
        UUID roomId,
        String customerName,
        LocalDate checkInDate,
        LocalDate checkOutDate) {}
