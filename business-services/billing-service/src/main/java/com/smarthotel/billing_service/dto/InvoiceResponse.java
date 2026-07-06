package com.smarthotel.billing_service.dto;

import com.smarthotel.billing_service.entity.Invoice;

import java.math.BigDecimal;
import java.util.UUID;

public record InvoiceResponse(
        UUID id, UUID bookingId,
        BigDecimal roomCharge, BigDecimal serviceCharge,
        BigDecimal tax, BigDecimal depositAmount, BigDecimal totalAmount, String status) {

    public static InvoiceResponse from(Invoice i) {
        return new InvoiceResponse(i.getId(), i.getBookingId(),
                i.getRoomCharge(), i.getServiceCharge(),
                i.getTax(), i.getDepositAmount(), i.getTotalAmount(), i.getStatus().name());
    }
}


