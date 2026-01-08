package com.rakeshgupta.cafebrew_backend.customer.service;

import com.rakeshgupta.cafebrew_backend.admin.dto.response.AdminOrderItemResponse;
import com.rakeshgupta.cafebrew_backend.admin.dto.response.AdminOrderResponse;
import com.rakeshgupta.cafebrew_backend.admin.service.OrderNotificationService;
import com.rakeshgupta.cafebrew_backend.customer.dto.request.PlaceOrderRequest;
import com.rakeshgupta.cafebrew_backend.customer.dto.response.PlaceOrderResponse;
import com.rakeshgupta.cafebrew_backend.customer.dto.response.TrackOrderResponse;
import com.rakeshgupta.cafebrew_backend.customer.entity.MenuItem;
import com.rakeshgupta.cafebrew_backend.customer.entity.Order;
import com.rakeshgupta.cafebrew_backend.customer.entity.OrderItem;
import com.rakeshgupta.cafebrew_backend.customer.entity.Payment;
import com.rakeshgupta.cafebrew_backend.customer.repository.MenuItemRepository;
import com.rakeshgupta.cafebrew_backend.customer.repository.OrderRepository;
import com.rakeshgupta.cafebrew_backend.customer.repository.PaymentRepository;
import com.rakeshgupta.cafebrew_backend.common.enums.OrderStatus;
import com.rakeshgupta.cafebrew_backend.common.enums.PaymentMode;
import com.rakeshgupta.cafebrew_backend.common.enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final OrderNotificationService orderNotificationService;
    
    /**
     * PLACE ORDER
     * Transactional because Order, OrderItems, and Payment must ALL succeed or ALL rollback.
     */
    @Transactional
    public PlaceOrderResponse placeOrder(PlaceOrderRequest request) {
        
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        
        Order order = new Order();
        order.setOrderCode(generateOrderCode());
        order.setCustomerName(request.getCustomerName());
        order.setCustomerPhone(request.getCustomerPhone());
        order.setStatus(OrderStatus.NEW);
        order.setPaymentMode(request.getPaymentMode());
        order.setPaymentStatus(PaymentStatus.PENDING);
        
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (PlaceOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            
            MenuItem menuItem = menuItemRepository.findByIdAndAvailableTrue(itemRequest.getMenuItemId())
                    .orElseThrow(() -> new IllegalStateException("Menu item not available: " + itemRequest.getMenuItemId()));
            
            if (itemRequest.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero");
            }
            
            BigDecimal itemTotal = menuItem.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setMenuItemId(menuItem.getId());
            orderItem.setMenuItemName(menuItem.getName());
            orderItem.setPrice(menuItem.getPrice());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setTotalPrice(itemTotal);
            
            orderItems.add(orderItem);
            totalAmount = totalAmount.add(itemTotal);
        }
        
        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);
        
        orderRepository.save(order);
        
        // Notify admin dashboard about new order via WebSocket
        orderNotificationService.notifyNewOrder(toAdminOrderResponse(order));
        
        // Create Payment record
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMode(order.getPaymentMode());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setAmount(totalAmount);
        
        paymentRepository.save(payment);
        
        String razorpayOrderId = null;
        
        if (order.getPaymentMode() == PaymentMode.ONLINE) {
            razorpayOrderId = paymentService.createOnlinePayment(order);
        }
        
        return new PlaceOrderResponse(
                order.getOrderCode(),
                order.getStatus(),
                order.getPaymentStatus(),
                order.getTotalAmount(),
                razorpayOrderId,
                "Order placed successfully"
        );
    }
    
    /**
     * PUBLIC ORDER TRACKING
     * No authentication, orderCode is the only identifier.
     */
    @Transactional(readOnly = true)
    public TrackOrderResponse trackOrder(String orderCode) {
        
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid order code"));
        
        List<TrackOrderResponse.OrderItemResponse> items = order.getItems().stream()
                .map(item -> new TrackOrderResponse.OrderItemResponse(
                        item.getMenuItemName(),
                        item.getQuantity()
                ))
                .toList();
        
        return new TrackOrderResponse(
                order.getOrderCode(),
                order.getCustomerName(),
                order.getStatus(),
                order.getPaymentStatus(),
                items
        );
    }
    
    /**
     * ADMIN ONLY
     * Order status state machine enforced here.
     */
    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        
        OrderStatus current = order.getStatus();
        
        if (!isValidTransition(current, newStatus)) {
            throw new IllegalStateException("Invalid status transition: " + current + " -> " + newStatus);
        }
        
        order.setStatus(newStatus);
        orderRepository.save(order);
    }
    
    /**
     * Order state machine
     */
    private boolean isValidTransition(OrderStatus current, OrderStatus next) {
        if (current == OrderStatus.NEW && next == OrderStatus.PREPARING) return true;
        if (current == OrderStatus.PREPARING && next == OrderStatus.READY) return true;
        if (current == OrderStatus.READY && next == OrderStatus.COMPLETED) return true;
        return false;
    }
    
    private String generateOrderCode() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * Convert Order entity to AdminOrderResponse DTO for WebSocket notifications
     */
    private AdminOrderResponse toAdminOrderResponse(Order order) {
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
}