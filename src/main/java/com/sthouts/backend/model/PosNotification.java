package com.sthouts.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "pos_notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tenantEmail;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(name = "notif_type")
    private String type; // e.g. "DISPATCH", "ALERT", "INFO"

    @Builder.Default
    private Boolean isRead = false;

    private String linkUrl; // e.g. "/branches"

    @CreationTimestamp
    private LocalDateTime createdAt;
}
