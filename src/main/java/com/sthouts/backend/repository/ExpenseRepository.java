package com.sthouts.backend.repository;

import com.sthouts.backend.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByTenantEmailOrderByExpenseDateDesc(String tenantEmail);
    List<Expense> findByTenantEmailAndExpenseDateOrderByCreatedAtDesc(String tenantEmail, LocalDate expenseDate);
}
