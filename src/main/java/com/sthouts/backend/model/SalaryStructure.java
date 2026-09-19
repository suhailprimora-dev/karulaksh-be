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
@Table(name = "salary_structures")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryStructure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long staffId;

    @Column(nullable = false)
    private Double basic;

    @Column(nullable = false)
    private Double hra;

    @Column(nullable = false)
    private Double foodAllowance;

    @Column(nullable = false)
    private Double travelAllowance;

    @Column(nullable = false)
    private Double pfDeduction;

    @Column(nullable = false)
    private Double taxDeduction;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private String tenantEmail;
}
