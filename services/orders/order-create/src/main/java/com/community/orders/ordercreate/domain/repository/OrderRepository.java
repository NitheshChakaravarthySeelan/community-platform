package com.community.orders.ordercreate.domain.repository;

import com.community.orders.ordercreate.domain.model.Order;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    // Spring Data JPA automatically provides CRUD methods (save, findById, findAll, delete, etc.)
    // You can define custom query methods here if needed, e.g.,
    // List<Order> findByUserId(UUID userId);
}
