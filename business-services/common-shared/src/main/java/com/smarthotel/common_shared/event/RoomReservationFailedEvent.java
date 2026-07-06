package com.smarthotel.common_shared.event;
import lombok.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomReservationFailedEvent {
    private UUID eventId;
    private UUID bookingId;
    private UUID roomId;
    private String failureReason;
}