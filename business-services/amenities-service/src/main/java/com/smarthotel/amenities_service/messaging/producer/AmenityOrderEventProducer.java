package com.smarthotel.amenities_service.messaging.producer;

import com.smarthotel.common_shared.event.AmenityOrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AmenityOrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderCreated(AmenityOrderCreatedEvent event) {
        String topic = "amenity-order-events";
        String partitionKey = event.getOrderId().toString();

        log.info("[Kafka Producer] Bắn event AmenityOrderCreatedEvent | OrderId: {} | Key: {}",
                event.getOrderId(), partitionKey);

        this.kafkaTemplate.send(topic, partitionKey, event);
    }
}

