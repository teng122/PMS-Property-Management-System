package com.smarthotel.billing.service;

import com.smarthotel.billing.dto.response.InvoiceResponse;
import com.smarthotel.billing.dto.response.PaymentInitResponse;
import com.smarthotel.billing.entity.Invoice;

import java.util.UUID;

public interface InvoiceService {

    /** Tao/khoi tao lai hoa don cho 1 booking (idempotent): tien phong + dich vu + VAT. */
    InvoiceResponse generate(UUID bookingId);

    /** Lay entity hoa don theo id (nem NoSuchElementException neu khong co). */
    Invoice findById(UUID id);

    /** Khoi tao thanh toan: sinh URL ma QR VietQR cho hoa don. */
    PaymentInitResponse initPayment(UUID id);

    /** Danh dau hoa don da thanh toan (PAID). */
    InvoiceResponse markPaid(UUID id);
}
