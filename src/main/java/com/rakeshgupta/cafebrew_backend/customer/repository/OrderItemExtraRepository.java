package com.rakeshgupta.cafebrew_backend.customer.repository;

import com.rakeshgupta.cafebrew_backend.customer.entity.OrderItemExtra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemExtraRepository extends JpaRepository<OrderItemExtra, Long> {

    /**
     * Find all extras for a specific order item.
     */
    List<OrderItemExtra> findByOrderItemId(Long orderItemId);

    /**
     * Check if any order item extras exist for a given extra ingredient ID.
     * Used to determine if an extra ingredient can be deleted.
     */
    boolean existsByExtraIngredientId(Long extraIngredientId);

    /**
     * Count how many order item extras reference a specific extra ingredient.
     */
    long countByExtraIngredientId(Long extraIngredientId);

    /**
     * Find all extras for order items belonging to a specific order.
     */
    @Query("SELECT oie FROM OrderItemExtra oie " +
           "JOIN oie.orderItem oi " +
           "WHERE oi.order.id = :orderId")
    List<OrderItemExtra> findByOrderId(@Param("orderId") Long orderId);
}
