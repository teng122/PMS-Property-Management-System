package com.smarthotel.booking_service.entity;
import com.smarthotel.booking_service.entity.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "check_in_date", nullable = false)
    private LocalDateTime checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDateTime checkOutDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private BookingStatus status;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "deposit_amount", nullable = false)
    private BigDecimal depositAmount;

    @Column(name = "is_deposit_paid", nullable = false)
    private Boolean isDepositPaid;

    @Version
    private Long version;
}