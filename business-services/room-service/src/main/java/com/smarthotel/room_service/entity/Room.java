package com.smarthotel.room_service.entity;

import jakarta.persistence.*;
import lombok.*;
import com.smarthotel.common_shared.model.RoomStatus;
import java.math.BigDecimal;
import java.util.UUID;


@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "room_number", unique = true, nullable = false, length = 20)
    private String roomNumber;

    @Column(name = "room_type", nullable = false, length = 30)
    private String roomType; // SINGLE, DOUBLE, SUITE

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private RoomStatus status;

    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice;

    @Column(name = "reserved_booking_id")
    private UUID reservedBookingId;

    private Integer floor;

    @Version
    private Long version;
}