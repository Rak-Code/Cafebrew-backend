package com.rakeshgupta.cafebrew_backend.customer.repository;

import com.rakeshgupta.cafebrew_backend.customer.entity.Order;
import com.rakeshgupta.cafebrew_backend.common.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * Find order by public order code for customer tracking
     */
    Optional<Order> findByOrderCode(String orderCode);
    
    /**
     * Find orders by status ordered by creation time (oldest first) for admin queue
     */
    List<Order> findByStatusOrderByCreatedAtAsc(OrderStatus status);

    /**
     * Find all orders ordered by creation time (newest first) for admin dashboard
     */
    List<Order> findAllByOrderByCreatedAtDesc();
}