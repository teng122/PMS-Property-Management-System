package com.smarthotel.billing.dto;

import java.math.BigDecimal;

public record PaymentInitResponse(String qrImageUrl, BigDecimal amount, String state) {}
