package com.smarthotel.billing.gateway.impl;

import com.smarthotel.billing.client.BookingClient;
import com.smarthotel.billing.dto.BookingInfoDTO;
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
    public BookingInfoDTO getBooking(UUID bookingId) {
        return client.getBooking(bookingId);
    }
}
