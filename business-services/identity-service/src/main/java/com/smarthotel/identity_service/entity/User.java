package com.smarthotel.identity_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String status = "ACTIVE"; // ACTIVE, BLOCKED

    @Column(name = "role", nullable = false, length = 30)
    private String role;

    @Column(name = "refresh_token", length = 255)
    private String refreshToken;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Fetch(FetchMode.SUBSELECT)
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Version
    private Long version;
}