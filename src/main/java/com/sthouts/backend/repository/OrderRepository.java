package com.sthouts.backend.repository;

import com.sthouts.backend.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByStatus(String status);
    List<Order> findByTenantEmail(String tenantEmail);
    List<Order> findByTenantEmailIsNull();
    Optional<Order> findByStatusAndTenantEmail(String status, String tenantEmail);
    Optional<Order> findByStatusAndTenantEmailIsNull(String status);
}
