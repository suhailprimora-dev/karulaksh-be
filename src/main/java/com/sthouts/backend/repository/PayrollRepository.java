package com.sthouts.backend.repository;

import com.sthouts.backend.model.PayrollRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRepository extends JpaRepository<PayrollRecord, Long> {
    List<PayrollRecord> findByMonth(String month);
    List<PayrollRecord> findByStaffId(Long staffId);
    List<PayrollRecord> findByStaffIdAndMonth(Long staffId, String month);
    Optional<PayrollRecord> findFirstByStaffIdAndMonth(Long staffId, String month);
    List<PayrollRecord> findByMonthAndTenantEmail(String month, String tenantEmail);
    List<PayrollRecord> findByMonthAndTenantEmailIsNull(String month);
}
