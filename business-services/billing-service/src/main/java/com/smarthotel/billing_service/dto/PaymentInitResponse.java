package com.smarthotel.billing_service.dto;

import java.math.BigDecimal;

public record PaymentInitResponse(String qrImageUrl, BigDecimal amount, String state) {}


