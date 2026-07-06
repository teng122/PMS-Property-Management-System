package com.smarthotel.common_shared.event;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingCreatedEvent {
    private UUID eventId;
    private UUID bookingId;
    private UUID roomId;
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
}