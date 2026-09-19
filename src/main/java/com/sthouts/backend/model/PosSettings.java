package com.sthouts.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pos_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String tenantEmail;

    @Builder.Default
    private Integer lowStockThreshold = 20; // default threshold (20, 50, 100 etc)

    @Builder.Default
    private String currency = "INR";

    private String receiptHeader;
    private String receiptFooter;

    @Builder.Default
    private Double taxRate = 5.0;
}
