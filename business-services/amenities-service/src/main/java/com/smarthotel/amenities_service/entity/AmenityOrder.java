package com.smarthotel.amenities_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "amenity_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmenityOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private AmenityOrderStatus status;


    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    @OneToMany(mappedBy = "amenityOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AmenityOrderDetail> details = new ArrayList<>();

    @Version
    private Long version;
}
