package com.smarthotel.billing.gateway.mock;

import com.smarthotel.billing.dto.BookingBillingDTO;
import com.smarthotel.billing.gateway.BookingGateway;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
@Profile("mock")
public class BookingGatewayMock implements BookingGateway {

    // roomId co dinh de khop voi AmenityGatewayMock
    public static final UUID MOCK_ROOM_ID =
            UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Override
    public BookingBillingDTO getBillingInfo(UUID bookingId) {
        return new BookingBillingDTO(
                bookingId,
                MOCK_ROOM_ID,
                "Nguyen Van A",
                LocalDate.now(),
                LocalDate.now().plusDays(2),
                new BigDecimal("1500000")    // tien phong mock = 1.500.000
        );
    }
}
