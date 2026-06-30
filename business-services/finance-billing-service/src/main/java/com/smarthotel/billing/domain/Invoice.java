package com.smarthotel.billing.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "invoices")
@Getter
@Setter
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    @Column(name = "room_charge")
    private BigDecimal roomCharge = BigDecimal.ZERO;

    @Column(name = "service_charge")
    private BigDecimal serviceCharge = BigDecimal.ZERO;

    private BigDecimal tax = BigDecimal.ZERO;

    @Column(name = "total_amount")
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status = InvoiceStatus.UNPAID;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Column(name = "paid_at")
    private Instant paidAt;
}
