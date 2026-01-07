package com.rakeshgupta.cafebrew_backend.admin.controller;

import com.rakeshgupta.cafebrew_backend.admin.dto.request.UpdateOrderStatusRequest;
import com.rakeshgupta.cafebrew_backend.admin.dto.response.AdminOrderResponse;
import com.rakeshgupta.cafebrew_backend.admin.service.AdminOrderService;
import com.rakeshgupta.cafebrew_backend.common.enums.OrderStatus;
import com.rakeshgupta.cafebrew_backend.customer.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    /**
     * GET /api/admin/orders or /api/admin/orders?status=NEW
     * Used for kitchen screen / order queue
     * If status is not provided, returns all orders
     */
    @GetMapping
    public ResponseEntity<List<AdminOrderResponse>> getOrdersByStatus(
            @RequestParam(required = false) OrderStatus status
    ) {
        List<Order> orders = (status != null) 
                ? adminOrderService.getOrdersByStatus(status)
                : adminOrderService.getAllOrders();

        List<AdminOrderResponse> response = orders.stream()
                .map(order -> new AdminOrderResponse(
                        order.getId(),
                        order.getOrderCode(),
                        order.getCustomerName(),
                        order.getStatus(),
                        order.getTotalAmount(),
                        order.getCreatedAt()
                ))
                .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/admin/orders/{orderId}/status
     * Enforces state machine inside service
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<Void> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody UpdateOrderStatusRequest request
    ) {
        adminOrderService.updateOrderStatus(orderId, request.getStatus());
        return ResponseEntity.ok().build();
    }

    /**
     * PUT /api/admin/orders/{orderId}/complete
     * Shortcut endpoint
     */
    @PutMapping("/{orderId}/complete")
    public ResponseEntity<Void> completeOrder(@PathVariable Long orderId) {
        adminOrderService.updateOrderStatus(orderId, OrderStatus.COMPLETED);
        return ResponseEntity.ok().build();
    }
}
