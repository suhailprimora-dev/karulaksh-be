package com.sthouts.backend.repository;

import com.sthouts.backend.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByTenantEmail(String tenantEmail);
    List<MenuItem> findByTenantEmailIsNull();
}
