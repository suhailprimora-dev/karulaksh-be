package com.sthouts.backend.repository;

import com.sthouts.backend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
    boolean existsByName(String name);
    List<Category> findByTenantEmail(String tenantEmail);
    List<Category> findByTenantEmailIsNull();
    Optional<Category> findByNameAndTenantEmail(String name, String tenantEmail);
    boolean existsByNameAndTenantEmail(String name, String tenantEmail);
}
