package com.smarthotel.billing.gateway;

import com.smarthotel.billing.dto.BookingBillingDTO;

import java.util.UUID;

public interface BookingGateway {
    BookingBillingDTO getBillingInfo(UUID bookingId);
}
