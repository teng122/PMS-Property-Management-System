package com.smarthotel.housekeeping_service.messaging.consumer;

import com.smarthotel.common_shared.event.CheckoutStartedEvent;
import com.smarthotel.housekeeping_service.entity.CleaningTask;
import com.smarthotel.housekeeping_service.entity.CleaningTaskStatus;
import com.smarthotel.housekeeping_service.repository.CleaningTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class HousekeepingSagaConsumer {

    private final CleaningTaskRepository cleaningTaskRepository;

    @KafkaListener(topics = "checkout-events", groupId = "housekeeping-group")
    @Transactional
    public void handleCheckoutStartedEvent(CheckoutStartedEvent event) {
        log.info("[Kafka Consumer] Nhận CheckoutStartedEvent | Tạo CleaningTask cho phòng {}", event.getRoomId());

        CleaningTask task = new CleaningTask();
        task.setRoomId(event.getRoomId());
        task.setStatus(CleaningTaskStatus.PENDING);
        task.setAssignedAt(null);

        cleaningTaskRepository.save(task);

        // Giả lập gửi thông báo WebSocket tới thiết bị của các STAFF
        log.info("[WebSocket Mock] Đã gửi thông báo dọn phòng {} tới tất cả nhân viên dọn phòng (STAFF)!", event.getRoomId());
    }
}
