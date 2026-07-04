package com.smarthotel.billing.dto.response;

import java.math.BigDecimal;

public record PaymentInitResponse(String qrImageUrl, BigDecimal amount, String state) {}
