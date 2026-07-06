package com.smarthotel.identity_service.entity;
import com.smarthotel.identity_service.entity.RoleName;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, unique = true, nullable = false)
    private RoleName name;
}