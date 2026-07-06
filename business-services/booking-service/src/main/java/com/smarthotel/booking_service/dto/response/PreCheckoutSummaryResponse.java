package com.smarthotel.booking_service.dto.response;

import com.smarthotel.booking_service.dto.external.InvoiceDto;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class PreCheckoutSummaryResponse {
    private UUID bookingId;
    private String customerName;
    private String roomNumber;
    private InvoiceDto invoice;
    private String paymentStatus;
}
