package com.sthouts.backend.service;

import com.sthouts.backend.config.TenantContext;
import com.sthouts.backend.model.PosSettings;
import com.sthouts.backend.repository.PosSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PosSettingsService {

    private final PosSettingsRepository repository;

    @Transactional
    public PosSettings getSettings() {
        String email = TenantContext.getTenantEmail();
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Unauthorized: Missing tenant context");
        }
        return repository.findByTenantEmail(email)
                .orElseGet(() -> repository.save(PosSettings.builder()
                        .tenantEmail(email)
                        .lowStockThreshold(20)
                        .currency("INR")
                        .taxRate(5.0)
                        .build()));
    }

    @Transactional
    public PosSettings updateSettings(PosSettings updated) {
        String email = TenantContext.getTenantEmail();
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Unauthorized");
        }
        PosSettings existing = repository.findByTenantEmail(email)
                .orElseGet(() -> PosSettings.builder().tenantEmail(email).build());

        existing.setLowStockThreshold(updated.getLowStockThreshold());
        existing.setCurrency(updated.getCurrency());
        existing.setReceiptHeader(updated.getReceiptHeader());
        existing.setReceiptFooter(updated.getReceiptFooter());
        existing.setTaxRate(updated.getTaxRate());

        return repository.save(existing);
    }
}
