package com.smarthotel.billing.web;

import com.smarthotel.billing.domain.Invoice;
import com.smarthotel.billing.dto.GenerateRequest;
import com.smarthotel.billing.dto.InvoiceResponse;
import com.smarthotel.billing.dto.PaymentInitResponse;
import com.smarthotel.billing.service.InvoiceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService service;

    public InvoiceController(InvoiceService service) {
        this.service = service;
    }

    @PostMapping("/generate")
    public ResponseEntity<InvoiceResponse> generate(@Valid @RequestBody GenerateRequest req) {
        return ResponseEntity.ok(service.generate(req.bookingId()));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<PaymentInitResponse> pay(@PathVariable UUID id) {
        return ResponseEntity.ok(service.initPayment(id));
    }

    @PostMapping("/{id}/confirm-payment")
    public ResponseEntity<InvoiceResponse> confirm(@PathVariable UUID id) {
        return ResponseEntity.ok(service.markPaid(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> get(@PathVariable UUID id) {
        Invoice inv = service.findById(id);
        return ResponseEntity.ok(InvoiceResponse.from(inv));
    }
}
