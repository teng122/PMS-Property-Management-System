package com.smarthotel.amenities_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "amenities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Amenity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false, length = 30)
    private String type; // FOOD, LAUNDRY, SPA

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private AmenityStatus status;

    @Column(name = "is_returnable", nullable = false, columnDefinition = "boolean default false")
    private Boolean isReturnable = false;

    @Version
    private Long version;
}
