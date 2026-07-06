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
public class AmenityOrderCreatedEvent {
    private UUID eventId;
    private UUID orderId;
    private UUID bookingId;
    private UUID roomId;
    private BigDecimal totalPrice;
    private LocalDateTime timestamp;
}
