package com.smarthotel.billing_service.messaging.consumer;

import com.smarthotel.common_shared.event.CheckoutStartedEvent;
import com.smarthotel.billing_service.entity.Invoice;
import com.smarthotel.billing_service.entity.InvoiceStatus;
import com.smarthotel.billing_service.client.AmenityClient;
import com.smarthotel.billing_service.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class BillingSagaConsumer {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.10");

    private final InvoiceRepository invoiceRepository;
    private final AmenityClient amenityClient;

    @KafkaListener(topics = "checkout-events", groupId = "billing-group")
    @Transactional
    public void handleCheckoutStartedEvent(CheckoutStartedEvent event) {
        log.info("[Kafka Consumer] Nhận CheckoutStartedEvent | Tạo Invoice cho booking {}", event.getBookingId());

        // Kiểm tra xem hóa đơn cho Booking này đã tồn tại chưa (Idempotency)
        if (invoiceRepository.findByBookingId(event.getBookingId()).isPresent()) {
            log.warn("[Idempotency] Hóa đơn cho Booking {} đã được tạo từ trước. Bỏ qua.", event.getBookingId());
            return;
        }

        // 1. Lấy tổng tiền dịch vụ phòng chưa trả từ amenities-service
        BigDecimal serviceCharge = BigDecimal.ZERO;
        try {
            serviceCharge = amenityClient.getUnpaidCharge(event.getBookingId());
        } catch (Exception e) {
            log.error("Không thể lấy tiền dịch vụ phòng cho booking {}: {}", event.getBookingId(), e.getMessage());
        }

        BigDecimal roomCharge = event.getRoomCharge();
        BigDecimal subtotal = roomCharge.add(serviceCharge);
        BigDecimal tax = subtotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = subtotal.add(tax);

        // 2. Tạo hóa đơn trạng thái UNPAID
        Invoice inv = new Invoice();
        inv.setBookingId(event.getBookingId());
        inv.setRoomCharge(roomCharge);
        inv.setServiceCharge(serviceCharge);
        inv.setTax(tax);
        inv.setTotalAmount(totalAmount);
        inv.setStatus(InvoiceStatus.UNPAID);

        invoiceRepository.save(inv);
        log.info("[Invoice Created] Đã lưu hóa đơn UNPAID cho booking {} với tổng tiền {}", event.getBookingId(), totalAmount);
    }
}


