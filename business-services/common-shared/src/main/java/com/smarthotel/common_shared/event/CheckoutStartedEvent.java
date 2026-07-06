package com.smarthotel.common_shared.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutStartedEvent {
    private UUID eventId;
    private UUID bookingId;
    private UUID roomId;
    private UUID customerId;
    private BigDecimal roomCharge;
    private LocalDateTime timestamp;
}
