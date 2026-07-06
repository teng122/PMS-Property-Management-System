package com.smarthotel.booking_service.dto.response;

import com.smarthotel.booking_service.entity.BookingStatus;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {
    private UUID id;
    private UUID roomId;
    private UUID customerId;
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private BookingStatus status;
}
