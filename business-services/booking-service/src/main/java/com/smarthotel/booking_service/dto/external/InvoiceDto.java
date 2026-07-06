package com.smarthotel.booking_service.dto.external;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class InvoiceDto {
    private UUID id;
    private UUID bookingId;
    private BigDecimal roomCharge;
    private BigDecimal serviceCharge;
    private BigDecimal tax;
    private BigDecimal totalAmount;
    private String status;
}
