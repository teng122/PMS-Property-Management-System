package com.smarthotel.billing_service.service;

import com.smarthotel.billing_service.dto.InvoiceResponse;
import com.smarthotel.billing_service.dto.PaymentInitResponse;
import com.smarthotel.billing_service.entity.Invoice;
import com.smarthotel.billing_service.entity.InvoiceStatus;
import com.smarthotel.billing_service.repository.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Service xử lý các nghiệp vụ liên quan đến hóa đơn và thanh toán (Billing & Payment).
 * Các hàm được tổ chức theo quy trình thực tế: Tạo hóa đơn -> Thanh toán -> Tra cứu.
 */
@Service
public class InvoiceService {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.10");
    private static final BigDecimal VND_THOUSAND_UNIT_FACTOR = new BigDecimal("1000");

    private final InvoiceRepository repo;

    public InvoiceService(InvoiceRepository repo) {
        this.repo = repo;
    }

    // ==========================================
    // 2. QUY TRÌNH THANH TOÁN (PAYMENT FLOW)
    // ==========================================

    /**
     * Khởi tạo quá trình thanh toán, sinh mã VietQR dựa trên số tiền hóa đơn.
     */
    public PaymentInitResponse initPayment(UUID id) {
        Invoice inv = findById(id);
        if (inv.getTotalAmount().compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal refundAmount = normalizeToVnd(inv.getTotalAmount().negate());
            return new PaymentInitResponse("REFUND_REQUIRED", refundAmount, "REFUND_PENDING");
        }
        BigDecimal paymentAmount = normalizeToVnd(inv.getTotalAmount());
        String qrUrl = "https://img.vietqr.io/image/970415-113366668888-compact2.png"
                + "?amount=" + paymentAmount.toPlainString()
                + "&addInfo=INV" + inv.getId();
        return new PaymentInitResponse(qrUrl, paymentAmount, "WAITING_BANK");
    }

    private BigDecimal normalizeToVnd(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }

        return amount.multiply(VND_THOUSAND_UNIT_FACTOR)
                .setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * Xác nhận thanh toán hóa đơn thành công và chuyển trạng thái hóa đơn sang PAID.
     */
    @Transactional
    public InvoiceResponse markPaid(UUID id) {
        Invoice inv = findById(id);
        if (inv.getStatus() == InvoiceStatus.PAID) {
            throw new IllegalStateException("Hoa don da thanh toan");
        }
        inv.setStatus(InvoiceStatus.PAID);
        inv.setPaidAt(Instant.now());
        return InvoiceResponse.from(repo.save(inv));
    }

    // ==========================================
    // 3. TRA CỨU HÓA ĐƠN (INVOICE QUERIES)
    // ==========================================

    /**
     * Lấy toàn bộ danh sách hóa đơn trong hệ thống.
     */
    public List<InvoiceResponse> getAllInvoices() {
        return repo.findAll().stream()
                .map(InvoiceResponse::from)
                .toList();
    }

    /**
     * Thống kê doanh thu cho Dashboard: tổng tiền phòng, dịch vụ, thuế và doanh thu (tính trên hóa đơn PAID).
     */
    public com.smarthotel.billing_service.dto.RevenueStatsResponse getRevenueStats() {
        List<Invoice> all = repo.findAll();
        List<Invoice> paid = repo.findByStatus(InvoiceStatus.PAID);

        BigDecimal roomRevenue = paid.stream().map(Invoice::getRoomCharge).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal serviceRevenue = paid.stream().map(Invoice::getServiceCharge).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal taxTotal = paid.stream().map(Invoice::getTax).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalRevenue = roomRevenue.add(serviceRevenue).add(taxTotal);

        return new com.smarthotel.billing_service.dto.RevenueStatsResponse(
                all.size(),
                paid.size(),
                all.size() - paid.size(),
                roomRevenue,
                serviceRevenue,
                taxTotal,
                totalRevenue);
    }

    /**
     * Tra cứu hóa đơn bằng ID hóa đơn.
     */
    public Invoice findById(UUID id) {
        return repo.findByIdOrThrow(id);
    }

    /**
     * Tra cứu hóa đơn bằng ID đơn đặt phòng (bookingId).
     */
    public Invoice findByBookingId(UUID bookingId) {
        return repo.findByBookingId(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy hóa đơn cho booking ID: " + bookingId));
    }
}


