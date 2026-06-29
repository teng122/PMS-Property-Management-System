package com.smarthotel.billing.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GenerateRequest(@NotNull UUID bookingId) {}
