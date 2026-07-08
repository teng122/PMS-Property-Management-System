package com.smarthotel.booking_service.dto.response;

import com.smarthotel.booking_service.entity.Booking;
import com.smarthotel.booking_service.entity.BookingStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {
    private UUID id;
    private UUID customerId;
    private UUID roomId;
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private BookingStatus status;
    private BigDecimal totalAmount;
    private BigDecimal depositAmount;
    private Boolean isDepositPaid;

    public static BookingResponse from(Booking b) {
        return BookingResponse.builder()
                .id(b.getId())
                .customerId(b.getCustomerId())
                .roomId(b.getRoomId())
                .checkInDate(b.getCheckInDate())
                .checkOutDate(b.getCheckOutDate())
                .status(b.getStatus())
                .totalAmount(b.getTotalAmount())
                .depositAmount(b.getDepositAmount())
                .isDepositPaid(b.getIsDepositPaid())
                .build();
    }
}
