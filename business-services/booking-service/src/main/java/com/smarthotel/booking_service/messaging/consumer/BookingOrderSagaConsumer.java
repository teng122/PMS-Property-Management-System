package com.smarthotel.booking_service.messaging.consumer;

import com.smarthotel.common_shared.event.AmenityOrderCreatedEvent;
import com.smarthotel.common_shared.event.AmenityOrderValidatedEvent;
import com.smarthotel.booking_service.entity.Booking;
import com.smarthotel.booking_service.entity.BookingStatus;
import com.smarthotel.booking_service.messaging.BookingEventPublisher;
import com.smarthotel.booking_service.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class BookingOrderSagaConsumer {

    private final BookingRepository bookingRepository;
    private final BookingEventPublisher bookingEventPublisher;

    @KafkaListener(topics = "amenity-order-events", groupId = "booking-group")
    @Transactional(readOnly = true)
    public void handleAmenityOrderCreatedEvent(AmenityOrderCreatedEvent event) {
        log.info("[Kafka Consumer] Nhận AmenityOrderCreatedEvent | OrderId: {} | BookingId: {} | RoomId: {}", 
                event.getOrderId(), event.getBookingId(), event.getRoomId());

        boolean isValid = false;
        try {
            Booking booking = bookingRepository.findById(event.getBookingId()).orElse(null);
            if (booking != null) {
                // 1. Kiểm tra roomId khớp Booking
                boolean isRoomMatch = booking.getRoomId().equals(event.getRoomId());
                
                // 2. Trạng thái phải là CHECKED_IN
                boolean isCheckedIn = booking.getStatus() == BookingStatus.CHECKED_IN;

                if (isRoomMatch && isCheckedIn) {
                    isValid = true;
                } else {
                    log.warn("Xác thực đơn tiện ích {} THẤT BẠI: RoomMatch={}, CheckedIn={}", 
                             event.getOrderId(), isRoomMatch, isCheckedIn);
                }
            } else {
                log.warn("Xác thực đơn tiện ích {} THẤT BẠI: Không tìm thấy Booking ID {}", event.getOrderId(), event.getBookingId());
            }
        } catch (Exception e) {
            log.error("Lỗi khi xác thực đơn gọi tiện ích {}: {}", event.getOrderId(), e.getMessage());
        }

        // Bắn sự kiện kết quả validation
        AmenityOrderValidatedEvent validationEvent = AmenityOrderValidatedEvent.builder()
                .eventId(UUID.randomUUID())
                .orderId(event.getOrderId())
                .isValid(isValid)
                .timestamp(LocalDateTime.now())
                .build();
        
        bookingEventPublisher.publishAmenityOrderValidated(validationEvent);
    }
}
