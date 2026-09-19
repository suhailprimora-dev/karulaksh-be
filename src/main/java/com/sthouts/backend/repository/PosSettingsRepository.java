package com.sthouts.backend.repository;

import com.sthouts.backend.model.PosSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PosSettingsRepository extends JpaRepository<PosSettings, Long> {
    Optional<PosSettings> findByTenantEmail(String tenantEmail);
}
