package com.smarthotel.billing.gateway.impl;

import com.smarthotel.billing.client.BookingClient;
import com.smarthotel.billing.dto.BookingBillingDTO;
import com.smarthotel.billing.gateway.BookingGateway;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("!mock")
public class BookingGatewayImpl implements BookingGateway {

    private final BookingClient client;

    public BookingGatewayImpl(BookingClient client) {
        this.client = client;
    }

    @Override
    public BookingBillingDTO getBillingInfo(UUID bookingId) {
        return client.getBillingInfo(bookingId);
    }
}
