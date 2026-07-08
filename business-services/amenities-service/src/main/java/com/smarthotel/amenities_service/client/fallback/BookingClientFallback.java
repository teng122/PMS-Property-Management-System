package com.smarthotel.amenities_service.client.fallback;

import com.smarthotel.amenities_service.client.BookingClient;
import com.smarthotel.amenities_service.dto.external.BookingDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
@Slf4j
public class BookingClientFallback implements BookingClient {

    @Override
    public BookingDto getActiveBookingByRoomId(UUID roomId) {
        log.warn("[Fallback] Booking Service is offline or timed out. Cannot resolve active booking for room ID: {}", roomId);
        return null;
    }
}
