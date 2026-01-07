package com.rakeshgupta.cafebrew_backend.admin.service;

import com.rakeshgupta.cafebrew_backend.customer.entity.Order;
import com.rakeshgupta.cafebrew_backend.customer.repository.OrderRepository;
import com.rakeshgupta.cafebrew_backend.common.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminOrderService {
    
    private final OrderRepository orderRepository;
    
    /**
     * Get all orders for admin dashboard
     * Returns orders sorted by creation time (newest first)
     */
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Get orders by status for admin queue management
     * Returns orders sorted by creation time (oldest first)
     */
    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatusOrderByCreatedAtAsc(status);
    }
    
    /**
     * Update order status (Admin operation)
     * Includes business rule validation
     */
    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        // BUSINESS RULE: Validate status transitions
        validateStatusTransition(order.getStatus(), newStatus);
        
        order.setStatus(newStatus);
        orderRepository.save(order);
    }
    
    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // Cannot update completed or cancelled orders
        if (currentStatus == OrderStatus.COMPLETED || currentStatus == OrderStatus.CANCELLED) {
            throw new RuntimeException("Cannot update status of completed or cancelled order");
        }
        
        // Add more business rules as needed
        // Example: NEW -> PREPARING -> READY -> COMPLETED
        // Or: NEW/PREPARING -> CANCELLED
    }
}