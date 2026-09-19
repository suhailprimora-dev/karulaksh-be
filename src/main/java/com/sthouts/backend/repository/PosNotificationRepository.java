package com.sthouts.backend.repository;

import com.sthouts.backend.model.PosNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PosNotificationRepository extends JpaRepository<PosNotification, Long> {
    List<PosNotification> findByTenantEmailOrderByCreatedAtDesc(String tenantEmail);
}
