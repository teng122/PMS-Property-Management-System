package com.smarthotel.common_shared.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmenityOrderValidatedEvent {
    private UUID eventId;
    private UUID orderId;
    private boolean isValid;
    private LocalDateTime timestamp;
}
