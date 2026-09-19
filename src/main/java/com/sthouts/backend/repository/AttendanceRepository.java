package com.sthouts.backend.repository;

import com.sthouts.backend.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByDate(LocalDate date);
    Optional<Attendance> findByStaffIdAndDate(Long staffId, LocalDate date);
    List<Attendance> findByDateAndTenantEmail(LocalDate date, String tenantEmail);
    List<Attendance> findByDateAndTenantEmailIsNull(LocalDate date);
    List<Attendance> findByTenantEmail(String tenantEmail);
}
