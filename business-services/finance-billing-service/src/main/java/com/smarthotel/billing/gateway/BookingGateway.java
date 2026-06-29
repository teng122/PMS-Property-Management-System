package com.smarthotel.billing.gateway;

import com.smarthotel.billing.dto.BookingInfoDTO;

import java.util.UUID;

public interface BookingGateway {

    /** Lay thong tin booking (chu yeu can roomId) tu S2 theo bookingId. */
    BookingInfoDTO getBooking(UUID bookingId);
}
