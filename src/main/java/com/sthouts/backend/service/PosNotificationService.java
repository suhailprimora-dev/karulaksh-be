package com.sthouts.backend.service;

import com.sthouts.backend.config.TenantContext;
import com.sthouts.backend.model.PosNotification;
import com.sthouts.backend.repository.PosNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PosNotificationService {

    private final PosNotificationRepository repository;

    public List<PosNotification> getNotifications() {
        String email = TenantContext.getTenantEmail();
        if (email == null) return Collections.emptyList();
        return repository.findByTenantEmailOrderByCreatedAtDesc(email);
    }

    public PosNotification createNotification(PosNotification notification) {
        String email = TenantContext.getTenantEmail();
        if (email == null) throw new RuntimeException("Unauthorized");
        notification.setTenantEmail(email);
        return repository.save(notification);
    }

    public void markAllAsRead() {
        String email = TenantContext.getTenantEmail();
        if (email == null) return;
        List<PosNotification> list = repository.findByTenantEmailOrderByCreatedAtDesc(email);
        list.forEach(n -> n.setIsRead(true));
        repository.saveAll(list);
    }
}
