package com.sthouts.backend.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String billNo;
    
    private String customerName;
    
    private String tableNo;

    private Double discount;

    private Double serviceCharge;

    private String paymentMethod;

    private Double gstRate;
    
    private Double subtotal;
    
    private Double totalAmount;
    
    private String status; // "ACTIVE" or "SETTLED"

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    @ToString.Exclude
    private List<OrderItem> items = new ArrayList<>();

    private String tenantEmail;
}
