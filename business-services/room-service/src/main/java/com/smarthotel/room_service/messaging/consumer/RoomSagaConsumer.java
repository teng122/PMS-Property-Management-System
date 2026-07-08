package com.smarthotel.room_service.messaging.consumer;

import com.smarthotel.common_shared.event.BookingCreatedEvent;
import com.smarthotel.common_shared.event.RoomReservationFailedEvent;
import com.smarthotel.common_shared.event.RoomReservedEvent;
import com.smarthotel.common_shared.event.CheckoutStartedEvent;
import com.smarthotel.common_shared.event.RoomCleanedEvent;
import com.smarthotel.room_service.entity.Room;
import com.smarthotel.common_shared.model.RoomStatus;

import com.smarthotel.room_service.messaging.RoomEventPublisher;
import com.smarthotel.room_service.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class RoomSagaConsumer {

    private final RoomRepository roomRepository;
    private final RoomEventPublisher roomEventPublisher;

    @KafkaListener(topics = "booking-events", groupId = "room-group")
    @Transactional
    public void handleBookingCreatedEvent(BookingCreatedEvent event) {
        log.info("[Kafka Consumer] Nhận BookingCreatedEvent | Xử lý phòng từ đơn: {}", event.getBookingId());

        Room room = roomRepository.findById(event.getRoomId())
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại với ID: " + event.getRoomId()));

        // Kiểm tra xem phòng có đang bảo trì không
        if (room.getStatus() == RoomStatus.MAINTENANCE) {
            RoomReservationFailedEvent failedEvent = RoomReservationFailedEvent.builder()
                    .eventId(UUID.randomUUID())
                    .bookingId(event.getBookingId())
                    .roomId(event.getRoomId())
                    .failureReason("Phòng đang trong trạng thái bảo trì (MAINTENANCE).")
                    .build();
            roomEventPublisher.publishRoomReservationFailed(failedEvent);
            return;
        }

        log.info("[DB Check] Phòng số {} hoạt động bình thường, giữ chỗ thành công", room.getRoomNumber());
        RoomReservedEvent successEvent = RoomReservedEvent.builder()
                .eventId(UUID.randomUUID())
                .bookingId(event.getBookingId())
                .roomId(event.getRoomId())
                .build();
        roomEventPublisher.publishRoomReserved(successEvent);
    }

    @KafkaListener(topics = "checkout-events", groupId = "room-group")
    @Transactional
    public void handleCheckoutStartedEvent(CheckoutStartedEvent event) {
        log.info("[Kafka Consumer] Nhận CheckoutStartedEvent | BookingId: {} | Đổi phòng {} sang DIRTY", 
                event.getBookingId(), event.getRoomId());
        Room room = roomRepository.findById(event.getRoomId())
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại với ID: " + event.getRoomId()));
        room.setStatus(RoomStatus.DIRTY);
        roomRepository.save(room);
    }

    @KafkaListener(topics = "housekeeping-events", groupId = "room-group")
    @Transactional
    public void handleRoomCleanedEvent(RoomCleanedEvent event) {
        log.info("[Kafka Consumer] Nhận RoomCleanedEvent | Đổi phòng {} sang AVAILABLE", event.getRoomId());
        Room room = roomRepository.findById(event.getRoomId())
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại với ID: " + event.getRoomId()));
        room.setStatus(RoomStatus.AVAILABLE);
        roomRepository.save(room);
    }
}
