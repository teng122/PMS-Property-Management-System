package com.smarthotel.amenities_service.messaging.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AmenityOrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishChargesCalculated(com.smarthotel.common_shared.event.AmenityChargesCalculatedEvent event) {
        String topic = "amenity-calculated-events";
        String partitionKey = event.getBookingId().toString();

        log.info("[Kafka Producer] Bắn event AmenityChargesCalculatedEvent | BookingId: {} | ServiceCharge: {}",
                event.getBookingId(), event.getServiceCharge());

        this.kafkaTemplate.send(topic, partitionKey, event);
    }
}
