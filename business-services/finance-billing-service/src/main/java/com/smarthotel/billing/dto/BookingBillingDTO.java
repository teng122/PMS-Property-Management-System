package com.smarthotel.billing.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BookingBillingDTO(
        UUID bookingId, UUID roomId, String customerName,
        LocalDate checkInDate, LocalDate checkOutDate,
        BigDecimal roomCharge) {}
