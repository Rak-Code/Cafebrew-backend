package com.rakeshgupta.cafebrew_backend.admin.service;

import com.rakeshgupta.cafebrew_backend.admin.dto.response.AdminOrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Notify all connected clients about a new order
     */
    public void notifyNewOrder(AdminOrderResponse order) {
        log.info("Broadcasting new order notification: {}", order.getOrderCode());
        messagingTemplate.convertAndSend("/topic/orders/new", order);
    }

    /**
     * Notify all connected clients about an order status update
     */
    public void notifyOrderStatusUpdate(AdminOrderResponse order) {
        log.info("Broadcasting order status update: {} -> {}", order.getOrderCode(), order.getStatus());
        messagingTemplate.convertAndSend("/topic/orders/status", order);
    }

    /**
     * Notify all connected clients to refresh their order list
     */
    public void notifyOrdersRefresh() {
        log.info("Broadcasting orders refresh notification");
        messagingTemplate.convertAndSend("/topic/orders/refresh", "refresh");
    }
}
