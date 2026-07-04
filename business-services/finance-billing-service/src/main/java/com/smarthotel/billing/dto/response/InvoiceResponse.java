package com.smarthotel.billing.dto.response;

import com.smarthotel.billing.entity.Invoice;

import java.math.BigDecimal;
import java.util.UUID;

public record InvoiceResponse(
        UUID id, UUID bookingId,
        BigDecimal roomCharge, BigDecimal serviceCharge,
        BigDecimal tax, BigDecimal totalAmount, String status) {

    public static InvoiceResponse from(Invoice i) {
        return new InvoiceResponse(i.getId(), i.getBookingId(),
                i.getRoomCharge(), i.getServiceCharge(),
                i.getTax(), i.getTotalAmount(), i.getStatus().name());
    }
}
