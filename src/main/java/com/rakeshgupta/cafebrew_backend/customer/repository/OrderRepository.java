package com.rakeshgupta.cafebrew_backend.customer.repository;

import com.rakeshgupta.cafebrew_backend.customer.entity.Order;
import com.rakeshgupta.cafebrew_backend.common.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    
    /**
     * Find orders by status with pagination
     */
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    
    /**
     * Search orders by customer name or order code (case-insensitive)
     */
    @Query("SELECT o FROM Order o WHERE " +
           "LOWER(o.customerName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(o.orderCode) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Order> searchByCustomerNameOrOrderCode(@Param("query") String query, Pageable pageable);
}