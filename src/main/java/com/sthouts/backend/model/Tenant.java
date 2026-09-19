package com.sthouts.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tenants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String businessName;

    @Column(nullable = false, unique = true)
    private String subdomain;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String sector;

    @Builder.Default
    private String plan = "ENTERPRISE_TRIAL";

    @Builder.Default
    private String status = "ACTIVE";

    @Builder.Default
    private String paymentStatus = "UNPAID"; // "UNPAID", "PAID", "EXEMPT"

    @Builder.Default
    private String role = "TENANT"; // "TENANT", "DEVELOPER", "SUPER_ADMIN"

    private LocalDateTime subscriptionExpiresAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
