package com.smarthotel.billing.client.fallback;

import com.smarthotel.billing.client.BookingClient;
import com.smarthotel.billing.dto.response.BookingInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Circuit-breaker fallback cho {@link BookingClient}.
 * Booking la du lieu bat buoc de tinh tien; khi khong lay duoc,
 * bao loi ro rang, nhanh (fail-fast) thay vi treo cho timeout.
 */
@Slf4j
@Component
public class BookingClientFallback implements BookingClient {

    @Override
    public BookingInfoDTO getBooking(UUID bookingId) {
        log.warn("[CircuitBreaker] booking-service khong kha dung -> fallback getBooking({})", bookingId);
        throw new IllegalStateException(
                "booking-service tam thoi khong kha dung, vui long thu lai sau (circuit breaker).");
    }
}
