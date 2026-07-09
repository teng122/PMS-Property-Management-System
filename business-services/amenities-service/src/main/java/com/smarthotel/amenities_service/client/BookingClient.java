package com.smarthotel.amenities_service.client;

import com.smarthotel.amenities_service.client.fallback.BookingClientFallback;
import com.smarthotel.amenities_service.dto.external.BookingDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

@FeignClient(
        name = "booking-service",
        url = "${clients.booking-service.url:http://booking-service:8082}",
        fallback = BookingClientFallback.class
)
public interface BookingClient {

    // Dùng endpoint đang được booking-service expose; FeignClientInterceptor sẽ chuyển tiếp X-User-* để giữ đúng phân quyền.
    @GetMapping("/api/bookings/active/room/{roomId}")
    BookingDto getActiveBookingByRoomId(@PathVariable("roomId") UUID roomId);
}
