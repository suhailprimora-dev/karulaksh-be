package com.sthouts.backend.repository;

import com.sthouts.backend.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    List<Staff> findByIsActiveTrue();
    List<Staff> findByTenantEmail(String tenantEmail);
    List<Staff> findByTenantEmailIsNull();
    List<Staff> findByIsActiveTrueAndTenantEmail(String tenantEmail);
    List<Staff> findByIsActiveTrueAndTenantEmailIsNull();
}
