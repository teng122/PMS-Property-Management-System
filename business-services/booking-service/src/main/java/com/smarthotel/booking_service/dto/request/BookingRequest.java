package com.smarthotel.booking_service.dto.request;

import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequest {
    private UUID roomId;
    private String customerName;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
}
