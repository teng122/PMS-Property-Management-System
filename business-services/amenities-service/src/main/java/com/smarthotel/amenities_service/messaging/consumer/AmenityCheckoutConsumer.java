package com.smarthotel.amenities_service.messaging.consumer;

import com.smarthotel.amenities_service.entity.AmenityOrder;
import com.smarthotel.amenities_service.entity.AmenityOrderStatus;
import com.smarthotel.amenities_service.messaging.producer.AmenityOrderEventProducer;
import com.smarthotel.amenities_service.repository.AmenityOrderRepository;
import com.smarthotel.common_shared.event.CheckoutStartedEvent;
import com.smarthotel.common_shared.event.AmenityChargesCalculatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class AmenityCheckoutConsumer {

    private final AmenityOrderRepository amenityOrderRepository;
    private final AmenityOrderEventProducer amenityOrderEventProducer;

    @KafkaListener(topics = "checkout-events", groupId = "amenities-group")
    @Transactional
    public void handleCheckoutStartedEvent(CheckoutStartedEvent event) {
        log.info("[Kafka Consumer] Nhận CheckoutStartedEvent | BookingId: {}", event.getBookingId());

        List<AmenityOrderStatus> unpaidStatuses = Arrays.asList(
                AmenityOrderStatus.PENDING,
                AmenityOrderStatus.PREPARING,
                AmenityOrderStatus.DELIVERED
        );

        List<AmenityOrder> unpaidOrders = amenityOrderRepository.findByBookingIdAndStatusIn(
                event.getBookingId(),
                unpaidStatuses
        );

        BigDecimal serviceCharge = BigDecimal.ZERO;
        for (AmenityOrder order : unpaidOrders) {
            serviceCharge = serviceCharge.add(order.getTotalPrice());
            order.setStatus(AmenityOrderStatus.BILLED);
            amenityOrderRepository.save(order);
        }

        log.info("[Amenities Billed] Đã gộp và đóng {} đơn dịch vụ sang BILLED cho booking {}, tổng tiền dịch vụ: {}", 
                unpaidOrders.size(), event.getBookingId(), serviceCharge);

        AmenityChargesCalculatedEvent calculatedEvent = AmenityChargesCalculatedEvent.builder()
                .eventId(UUID.randomUUID())
                .bookingId(event.getBookingId())
                .roomId(event.getRoomId())
                .customerId(event.getCustomerId())
                .roomCharge(event.getRoomCharge())
                .depositAmount(event.getDepositAmount() != null ? event.getDepositAmount() : BigDecimal.ZERO)
                .serviceCharge(serviceCharge)
                .timestamp(LocalDateTime.now())
                .build();

        amenityOrderEventProducer.publishChargesCalculated(calculatedEvent);
    }
}
