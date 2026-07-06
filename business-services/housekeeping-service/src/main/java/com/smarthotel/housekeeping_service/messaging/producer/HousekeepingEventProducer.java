package com.smarthotel.housekeeping_service.messaging.producer;

import com.smarthotel.common_shared.event.RoomCleanedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class HousekeepingEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishRoomCleaned(RoomCleanedEvent event) {
        String topic = "housekeeping-events";
        String partitionKey = event.getRoomId().toString();

        log.info("[Kafka Producer] Bắn event RoomCleanedEvent | RoomId: {} | Key: {}",
                event.getRoomId(), partitionKey);

        this.kafkaTemplate.send(topic, partitionKey, event);
    }
}
