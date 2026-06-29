package com.smarthotel.billing.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record UnpaidAmenityDTO(
        UUID orderId, String amenityName, int quantity, BigDecimal lineTotal) {}
