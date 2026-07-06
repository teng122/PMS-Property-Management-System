package com.smarthotel.amenities_service.messaging.consumer;

import com.smarthotel.common_shared.event.AmenityOrderValidatedEvent;
import com.smarthotel.amenities_service.entity.AmenityOrder;
import com.smarthotel.amenities_service.entity.AmenityOrderStatus;
import com.smarthotel.amenities_service.repository.AmenityOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class AmenityOrderSagaConsumer {

    private final AmenityOrderRepository amenityOrderRepository;

    @KafkaListener(topics = "booking-validation-events", groupId = "amenities-group")
    @Transactional
    public void handleAmenityOrderValidatedEvent(AmenityOrderValidatedEvent event) {
        log.info("[Kafka Consumer] Nhận AmenityOrderValidatedEvent | OrderId: {} | isValid: {}", 
                event.getOrderId(), event.isValid());

        AmenityOrder order = amenityOrderRepository.findById(event.getOrderId()).orElse(null);
        if (order == null) {
            log.error("Không tìm thấy đơn dịch vụ tiện ích với ID: {}", event.getOrderId());
            return;
        }

        // Kiểm tra bảo vệ Idempotency: Chỉ xử lý nếu đơn đang ở trạng thái PENDING
        if (order.getStatus() != AmenityOrderStatus.PENDING) {
            log.warn("[Idempotency] Đơn tiện ích {} đã được xử lý trước đó (Trạng thái hiện tại: {}). Bỏ qua.", 
                    order.getId(), order.getStatus());
            return;
        }

        if (event.isValid()) {
            order.setStatus(AmenityOrderStatus.PREPARING);
            amenityOrderRepository.save(order);
            log.info("[Order Validated] Đơn tiện ích {} đã chuyển sang trạng thái: PREPARING", order.getId());

            // Giả lập đẩy WebSocket xuống quầy dịch vụ/nhà bếp
            log.info("[WebSocket Mock] Đã đẩy thông báo tới bộ phận nhà bếp/dịch vụ chuẩn bị đơn hàng {} cho phòng {}!", 
                    order.getId(), order.getRoomId());
        } else {
            order.setStatus(AmenityOrderStatus.REJECTED);
            amenityOrderRepository.save(order);
            log.warn("[Order Rejected] Đơn tiện ích {} không hợp lệ và đã bị hủy (Trạng thái: REJECTED)", order.getId());
        }
    }
}

