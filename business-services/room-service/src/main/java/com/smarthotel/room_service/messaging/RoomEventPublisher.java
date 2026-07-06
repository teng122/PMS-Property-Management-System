package com.smarthotel.room_service.messaging;

import com.smarthotel.common_shared.event.RoomReservationFailedEvent;
import com.smarthotel.common_shared.event.RoomReservedEvent;

public interface RoomEventPublisher {
    void publishRoomReserved(RoomReservedEvent event);
    void publishRoomReservationFailed(RoomReservationFailedEvent event);
}
