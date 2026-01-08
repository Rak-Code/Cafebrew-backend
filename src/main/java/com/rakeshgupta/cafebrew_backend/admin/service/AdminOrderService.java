package com.rakeshgupta.cafebrew_backend.admin.service;

import com.rakeshgupta.cafebrew_backend.admin.dto.response.AdminOrderItemResponse;
import com.rakeshgupta.cafebrew_backend.admin.dto.response.AdminOrderResponse;
import com.rakeshgupta.cafebrew_backend.customer.entity.Order;
import com.rakeshgupta.cafebrew_backend.customer.repository.OrderRepository;
import com.rakeshgupta.cafebrew_backend.common.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminOrderService {
    
    private final OrderRepository orderRepository;
    private final OrderNotificationService orderNotificationService;
    
    // Valid status transitions map
    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = Map.of(
        OrderStatus.NEW, Set.of(OrderStatus.PREPARING, OrderStatus.CANCELLED),
        OrderStatus.PREPARING, Set.of(OrderStatus.READY, OrderStatus.CANCELLED),
        OrderStatus.READY, Set.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED),
        OrderStatus.COMPLETED, Set.of(),
        OrderStatus.CANCELLED, Set.of()
    );
    
    /**
     * Get all orders for admin dashboard with pagination
     */
    @Transactional(readOnly = true)
    public Page<Order> getAllOrdersPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return orderRepository.findAll(pageable);
    }
    
    /**
     * Get all orders (non-paginated for backward compatibility)
     */
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Get orders by status for admin queue management with pagination
     */
    @Transactional(readOnly = true)
    public Page<Order> getOrdersByStatusPaginated(OrderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        return orderRepository.findByStatus(status, pageable);
    }
    
    /**
     * Get orders by status (non-paginated for backward compatibility)
     */
    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatusOrderByCreatedAtAsc(status);
    }
    
    /**
     * Search orders by customer name or order code with pagination
     */
    @Transactional(readOnly = true)
    public Page<Order> searchOrders(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return orderRepository.searchByCustomerNameOrOrderCode(query, pageable);
    }
    
    /**
     * Update order status (Admin operation)
     * Includes business rule validation
     */
    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));
        
        // BUSINESS RULE: Validate status transitions
        validateStatusTransition(order.getStatus(), newStatus);
        
        order.setStatus(newStatus);
        orderRepository.save(order);
        
        // Notify connected clients about status update via WebSocket
        orderNotificationService.notifyOrderStatusUpdate(toAdminOrderResponse(order));
    }
    
    /**
     * Convert Order entity to AdminOrderResponse DTO
     */
    public AdminOrderResponse toAdminOrderResponse(Order order) {
        List<AdminOrderItemResponse> items = order.getItems().stream()
                .map(item -> new AdminOrderItemResponse(
                        item.getId(),
                        item.getMenuItemName(),
                        item.getPrice(),
                        item.getQuantity(),
                        item.getTotalPrice()
                ))
                .toList();
        
        return new AdminOrderResponse(
                order.getId(),
                order.getOrderCode(),
                order.getCustomerName(),
                order.getCustomerPhone(),
                order.getStatus(),
                order.getPaymentMode(),
                order.getPaymentStatus(),
                order.getTotalAmount(),
                items,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
    
    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // Cannot update completed or cancelled orders
        if (currentStatus == OrderStatus.COMPLETED || currentStatus == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot update status of completed or cancelled order");
        }
        
        // Check if transition is valid
        Set<OrderStatus> allowedTransitions = VALID_TRANSITIONS.get(currentStatus);
        if (allowedTransitions == null || !allowedTransitions.contains(newStatus)) {
            throw new IllegalStateException(
                String.format("Invalid status transition from %s to %s. Allowed transitions: %s",
                    currentStatus, newStatus, allowedTransitions)
            );
        }
    }
}