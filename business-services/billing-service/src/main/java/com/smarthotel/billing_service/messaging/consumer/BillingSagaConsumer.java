package com.smarthotel.billing_service.messaging.consumer;

import com.smarthotel.common_shared.event.CheckoutStartedEvent;
import com.smarthotel.billing_service.entity.Invoice;
import com.smarthotel.billing_service.entity.InvoiceStatus;
import com.smarthotel.billing_service.client.AmenityClient;
import com.smarthotel.billing_service.client.BookingClient;
import com.smarthotel.billing_service.dto.BookingInfoDTO;
import com.smarthotel.billing_service.dto.UnpaidAmenityDTO;
import com.smarthotel.billing_service.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class BillingSagaConsumer {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.10");

    private final InvoiceRepository invoiceRepository;
    private final AmenityClient amenityClient;
    private final BookingClient bookingClient;

    @KafkaListener(topics = "checkout-events", groupId = "billing-group")
    @Transactional
    public void handleCheckoutStartedEvent(CheckoutStartedEvent event) {
        log.info("[Kafka Consumer] Nhận CheckoutStartedEvent | Tạo Invoice cho booking {}", event.getBookingId());

        // Kiểm tra xem hóa đơn cho Booking này đã tồn tại chưa (Idempotency)
        if (invoiceRepository.findByBookingId(event.getBookingId()).isPresent()) {
            log.warn("[Idempotency] Hóa đơn cho Booking {} đã được tạo từ trước. Bỏ qua.", event.getBookingId());
            return;
        }

        // 1. Lấy danh sách dịch vụ phòng chưa trả (theo booking) để tính tiền và đóng đơn khi checkout
        List<UnpaidAmenityDTO> unpaidOrders;
        try {
            unpaidOrders = amenityClient.getUnpaidByBookingId(event.getBookingId());
        } catch (Exception e) {
            log.error("Không thể lấy dịch vụ phòng cho booking {}: {}", event.getBookingId(), e.getMessage());
            unpaidOrders = Collections.emptyList();
        }
        BigDecimal serviceCharge = unpaidOrders.stream()
                .map(UnpaidAmenityDTO::totalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Lấy tiền đặt cọc từ booking-service để trừ vào tổng cần thanh toán
        BigDecimal depositAmount = BigDecimal.ZERO;
        try {
            BookingInfoDTO booking = bookingClient.getBooking(event.getBookingId());
            if (booking != null && booking.depositAmount() != null) {
                depositAmount = booking.depositAmount();
            }
        } catch (Exception e) {
            log.error("Không thể lấy tiền cọc cho booking {}: {}", event.getBookingId(), e.getMessage());
        }

        // 3. Tính thuế VAT và tổng tiền (đã trừ cọc, tối thiểu 0)
        BigDecimal roomCharge = event.getRoomCharge();
        BigDecimal subtotal = roomCharge.add(serviceCharge);
        BigDecimal tax = subtotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = subtotal.add(tax).subtract(depositAmount);
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        // 4. Tạo hóa đơn trạng thái UNPAID
        Invoice inv = new Invoice();
        inv.setBookingId(event.getBookingId());
        inv.setRoomCharge(roomCharge);
        inv.setServiceCharge(serviceCharge);
        inv.setTax(tax);
        inv.setDepositAmount(depositAmount);
        inv.setTotalAmount(totalAmount);
        inv.setStatus(InvoiceStatus.UNPAID);

        invoiceRepository.save(inv);
        log.info("[Invoice Created] Đã lưu hóa đơn UNPAID cho booking {} (đã trừ cọc {}) với tổng tiền {}",
                event.getBookingId(), depositAmount, totalAmount);

        // 5. Đóng các đơn dịch vụ phòng đã gộp hóa đơn (chuyển sang BILLED)
        for (UnpaidAmenityDTO order : unpaidOrders) {
            try {
                amenityClient.updateOrderStatus(order.id(), "BILLED");
            } catch (Exception e) {
                log.error("Không thể đóng đơn dịch vụ {} sang BILLED: {}", order.id(), e.getMessage());
            }
        }
    }
}


