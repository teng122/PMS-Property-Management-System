package com.smarthotel.billing.gateway.mock;

import com.smarthotel.billing.dto.BookingInfoDTO;
import com.smarthotel.billing.gateway.BookingGateway;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
@Profile("mock")
public class BookingGatewayMock implements BookingGateway {

    // roomId co dinh de khop voi AmenityGatewayMock
    public static final UUID MOCK_ROOM_ID =
            UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Override
    public BookingInfoDTO getBooking(UUID bookingId) {
        return new BookingInfoDTO(
                bookingId,
                MOCK_ROOM_ID,
                "Nguyen Van A",
                LocalDate.now(),
                LocalDate.now().plusDays(2)
        );
    }
}
