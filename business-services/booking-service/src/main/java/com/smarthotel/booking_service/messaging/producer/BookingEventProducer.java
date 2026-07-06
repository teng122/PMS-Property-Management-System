package com.smarthotel.booking_service.messaging.producer;

import com.smarthotel.booking_service.messaging.BookingEventPublisher;
import com.smarthotel.common_shared.event.BookingCreatedEvent;
import com.smarthotel.common_shared.event.CheckoutStartedEvent;
import com.smarthotel.common_shared.event.AmenityOrderValidatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class BookingEventProducer implements BookingEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishBookingCreated(BookingCreatedEvent event) {
        String topic = "booking-events";
        String partitionKey = event.getBookingId().toString();

        log.info("[Kafka Producer] Bắn event lệnh giữ chỗ BookingCreatedEvent | BookingId: {} | Key: {}",
                event.getBookingId(), partitionKey);

        this.kafkaTemplate.send(topic, partitionKey, event);
    }

    @Override
    public void publishCheckoutStarted(CheckoutStartedEvent event) {
        String topic = "checkout-events";
        String partitionKey = event.getBookingId().toString();

        log.info("[Kafka Producer] Bắn event CheckoutStartedEvent | BookingId: {} | RoomId: {} | Key: {}",
                event.getBookingId(), event.getRoomId(), partitionKey);

        this.kafkaTemplate.send(topic, partitionKey, event);
    }

    @Override
    public void publishAmenityOrderValidated(AmenityOrderValidatedEvent event) {
        String topic = "booking-validation-events";
        String partitionKey = event.getOrderId().toString();

        log.info("[Kafka Producer] Bắn event AmenityOrderValidatedEvent | OrderId: {} | isValid: {} | Key: {}",
                event.getOrderId(), event.isValid(), partitionKey);

        this.kafkaTemplate.send(topic, partitionKey, event);
    }
}