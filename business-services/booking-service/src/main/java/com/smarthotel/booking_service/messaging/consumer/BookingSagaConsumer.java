package com.smarthotel.booking_service.messaging.consumer;

import com.smarthotel.booking_service.entity.Booking;
import com.smarthotel.booking_service.entity.BookingStatus;
import com.smarthotel.booking_service.repository.BookingRepository;
import com.smarthotel.common_shared.event.RoomReservationFailedEvent;
import com.smarthotel.common_shared.event.RoomReservedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
@KafkaListener(topics = "room-events", groupId = "booking-group")
public class BookingSagaConsumer {

    private final BookingRepository bookingRepository;

    /**
     * Luồng chạy xuôi: room-service giữ phòng thành công -> CONFIRMED
     */
    @KafkaHandler
    @Transactional
    public void handleRoomReservedEvent(RoomReservedEvent event) {
        log.info("[Kafka Consumer] Nhận RoomReservedEvent | Giữ phòng THÀNH CÔNG cho đơn: {}", event.getBookingId());

        Booking booking = bookingRepository.findById(event.getBookingId())
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại với ID: " + event.getBookingId()));

        // Bảo vệ Idempotency: Chỉ xử lý nếu đơn hàng vẫn đang ở trạng thái PENDING
        if (booking.getStatus() != BookingStatus.PENDING) {
            log.warn("[Idempotency] Đơn {} đã được xử lý trước đó (Trạng thái hiện tại: {}). Bỏ qua.", 
                    booking.getId(), booking.getStatus());
            return;
        }

        booking.setStatus(BookingStatus.AWAITING_DEPOSIT);
        bookingRepository.save(booking);
        log.info("[DB Update] Cập nhật Booking sang trạng thái: AWAITING_DEPOSIT");
    }

    /**
     * Luồng lệnh bù (Compensating): room-service hết phòng/bảo trì -> CANCELLED
     */
    @KafkaHandler
    @Transactional
    public void handleRoomReservationFailedEvent(RoomReservationFailedEvent event) {
        log.error("[Kafka Consumer] Nhận RoomReservationFailedEvent | Thất bại đơn: {}. Lý do: {}",
                event.getBookingId(), event.getFailureReason());

        Booking booking = bookingRepository.findById(event.getBookingId())
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại với ID: " + event.getBookingId()));

        // Bảo vệ Idempotency: Chỉ xử lý nếu đơn hàng vẫn đang ở trạng thái PENDING
        if (booking.getStatus() != BookingStatus.PENDING) {
            log.warn("[Idempotency] Đơn {} đã được xử lý trước đó (Trạng thái hiện tại: {}). Bỏ qua.", 
                    booking.getId(), booking.getStatus());
            return;
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        log.info("[DB Rollback] Lệnh bù chạy thành công. Booking đổi trạng thái: CANCELLED");
    }
}