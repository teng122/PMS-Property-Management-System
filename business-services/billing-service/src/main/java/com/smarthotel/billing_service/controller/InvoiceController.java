package com.smarthotel.billing_service.controller;

import com.smarthotel.billing_service.entity.Invoice;
import com.smarthotel.billing_service.dto.GenerateRequest;
import com.smarthotel.billing_service.dto.InvoiceResponse;
import com.smarthotel.billing_service.dto.PaymentInitResponse;
import com.smarthotel.billing_service.service.InvoiceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.UUID;

/**
 * Controller quản lý chu trình hóa đơn và thanh toán (Billing & Payment).
 * Sắp xếp các API theo quy trình thực tế: Tạo hóa đơn -> Yêu cầu thanh toán -> Xác nhận thanh toán -> Tra cứu hóa đơn.
 */
@RestController
@RequestMapping("/api/invoices")
@PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
public class InvoiceController {

    private final InvoiceService service;

    public InvoiceController(InvoiceService service) {
        this.service = service;
    }

    // ==========================================
    // 1. TẠO HÓA ĐƠN (INVOICE GENERATION)
    // ==========================================

    /**
     * Tạo hóa đơn tính tiền cho khách dựa trên đơn đặt phòng và các dịch vụ phòng đi kèm chưa thanh toán.
     */
    @PostMapping("/generate")
    public ResponseEntity<InvoiceResponse> generate(@Valid @RequestBody GenerateRequest req) {
        return ResponseEntity.ok(service.generate(req.bookingId()));
    }

    // ==========================================
    // 2. QUY TRÌNH THANH TOÁN (PAYMENT FLOW)
    // ==========================================

    /**
     * Khởi tạo quá trình thanh toán cho hóa đơn (ví dụ: xuất link thanh toán, mã QR).
     */
    @PostMapping("/{id}/pay")
    public ResponseEntity<PaymentInitResponse> pay(@PathVariable UUID id) {
        return ResponseEntity.ok(service.initPayment(id));
    }

    /**
     * Xác nhận thanh toán hóa đơn thành công (chuyển trạng thái hóa đơn sang PAID).
     */
    @PostMapping("/{id}/confirm-payment")
    public ResponseEntity<InvoiceResponse> confirm(@PathVariable UUID id) {
        return ResponseEntity.ok(service.markPaid(id));
    }

    // ==========================================
    // 3. TRA CỨU HÓA ĐƠN (INVOICE QUERIES)
    // ==========================================

    /**
     * Tra cứu thông tin chi tiết của một hóa đơn bằng ID hóa đơn.
     */
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> get(@PathVariable UUID id) {
        Invoice inv = service.findById(id);
        return ResponseEntity.ok(InvoiceResponse.from(inv));
    }

    /**
     * Tra cứu thông tin hóa đơn dựa trên ID đơn đặt phòng (bookingId).
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<InvoiceResponse> getByBookingId(@PathVariable UUID bookingId) {
        Invoice inv = service.findByBookingId(bookingId);
        return ResponseEntity.ok(InvoiceResponse.from(inv));
    }
}


