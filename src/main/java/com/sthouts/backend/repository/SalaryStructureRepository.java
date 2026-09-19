package com.sthouts.backend.repository;

import com.sthouts.backend.model.SalaryStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalaryStructureRepository extends JpaRepository<SalaryStructure, Long> {
    Optional<SalaryStructure> findByStaffId(Long staffId);
    List<SalaryStructure> findByTenantEmail(String tenantEmail);
}
