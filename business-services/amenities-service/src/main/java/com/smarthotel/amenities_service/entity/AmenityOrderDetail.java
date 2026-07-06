package com.smarthotel.amenities_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "amenity_order_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmenityOrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private AmenityOrder amenityOrder;

    @Column(name = "amenity_id", nullable = false)
    private UUID amenityId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal price; // Lưu giá tại thời điểm đặt đề phòng thay đổi giá danh mục
}
