package com.rakeshgupta.cafebrew_backend.admin.controller;

import com.rakeshgupta.cafebrew_backend.admin.dto.request.UpdateOrderStatusRequest;
import com.rakeshgupta.cafebrew_backend.admin.dto.response.AdminOrderResponse;
import com.rakeshgupta.cafebrew_backend.admin.service.AdminOrderService;
import com.rakeshgupta.cafebrew_backend.common.enums.OrderStatus;
import com.rakeshgupta.cafebrew_backend.customer.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
     * Supports pagination with page and size params
     * Supports search with query param
     */
    @GetMapping
    public ResponseEntity<?> getOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean paginated
    ) {
        // Search takes priority
        if (query != null && !query.trim().isEmpty()) {
            Page<Order> ordersPage = adminOrderService.searchOrders(query.trim(), page, size);
            Page<AdminOrderResponse> response = ordersPage.map(adminOrderService::toAdminOrderResponse);
            return ResponseEntity.ok(response);
        }
        
        // Paginated response
        if (paginated) {
            Page<Order> ordersPage = (status != null)
                    ? adminOrderService.getOrdersByStatusPaginated(status, page, size)
                    : adminOrderService.getAllOrdersPaginated(page, size);
            
            Page<AdminOrderResponse> response = ordersPage.map(adminOrderService::toAdminOrderResponse);
            return ResponseEntity.ok(response);
        }
        
        // Non-paginated response (backward compatibility)
        List<Order> orders = (status != null) 
                ? adminOrderService.getOrdersByStatus(status)
                : adminOrderService.getAllOrders();

        List<AdminOrderResponse> response = orders.stream()
                .map(adminOrderService::toAdminOrderResponse)
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
