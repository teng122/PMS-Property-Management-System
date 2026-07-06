package com.smarthotel.room_service.messaging.producer;

import com.smarthotel.common_shared.event.RoomReservationFailedEvent;
import com.smarthotel.common_shared.event.RoomReservedEvent;
import com.smarthotel.room_service.messaging.RoomEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class RoomEventProducer implements RoomEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "room-events";

    @Override
    public void publishRoomReserved(RoomReservedEvent event) {
        String partitionKey = event.getBookingId().toString();
        log.info("[Kafka Producer] Bắn event giữ phòng thành công RoomReservedEvent | BookingId: {} | Key: {}",
                event.getBookingId(), partitionKey);
        kafkaTemplate.send(TOPIC, partitionKey, event);
    }

    @Override
    public void publishRoomReservationFailed(RoomReservationFailedEvent event) {
        String partitionKey = event.getBookingId().toString();
        log.error("[Kafka Producer] Bắn event giữ phòng thất bại RoomReservationFailedEvent | BookingId: {} | Reason: {}",
                event.getBookingId(), event.getFailureReason());
        kafkaTemplate.send(TOPIC, partitionKey, event);
    }
}
