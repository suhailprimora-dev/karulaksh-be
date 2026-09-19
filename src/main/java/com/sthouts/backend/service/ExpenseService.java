package com.sthouts.backend.service;

import com.sthouts.backend.config.TenantContext;
import com.sthouts.backend.model.Expense;
import com.sthouts.backend.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public Expense createExpense(Expense expense) {
        String tenantEmail = TenantContext.getTenantEmail();
        if (tenantEmail == null || tenantEmail.trim().isEmpty()) {
            throw new RuntimeException("Unauthorized: Missing tenant context");
        }
        
        expense.setTenantEmail(tenantEmail);
        return expenseRepository.save(expense);
    }

    public List<Expense> getTodayExpenses() {
        String tenantEmail = TenantContext.getTenantEmail();
        if (tenantEmail == null || tenantEmail.trim().isEmpty()) {
            return List.of();
        }
        
        return expenseRepository.findByTenantEmailAndExpenseDateOrderByCreatedAtDesc(tenantEmail, LocalDate.now());
    }

    public List<Expense> getAllExpenses() {
        String tenantEmail = TenantContext.getTenantEmail();
        if (tenantEmail == null || tenantEmail.trim().isEmpty()) {
            return List.of();
        }
        
        return expenseRepository.findByTenantEmailOrderByExpenseDateDesc(tenantEmail);
    }

    public void deleteExpense(Long id) {
        String tenantEmail = TenantContext.getTenantEmail();
        if (tenantEmail == null || tenantEmail.trim().isEmpty()) {
            throw new RuntimeException("Unauthorized: Missing tenant context");
        }
        
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
                
        if (!expense.getTenantEmail().equals(tenantEmail)) {
            throw new RuntimeException("Unauthorized: Cannot delete expense for this tenant");
        }
        
        expenseRepository.deleteById(id);
    }
}
