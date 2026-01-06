package com.rakeshgupta.cafebrew_backend.customer.controller;

import com.rakeshgupta.cafebrew_backend.customer.dto.request.PlaceOrderRequest;
import com.rakeshgupta.cafebrew_backend.customer.dto.response.PlaceOrderResponse;
import com.rakeshgupta.cafebrew_backend.customer.dto.response.TrackOrderResponse;
import com.rakeshgupta.cafebrew_backend.customer.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * POST /api/orders
     * Place a new order (COD or ONLINE)
     * Public endpoint
     */
    @PostMapping
    public ResponseEntity<PlaceOrderResponse> placeOrder(
            @Valid @RequestBody PlaceOrderRequest request
    ) {
        PlaceOrderResponse response = orderService.placeOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/orders/track/{orderCode}
     * Public order tracking without authentication
     */
    @GetMapping("/track/{orderCode}")
    public ResponseEntity<TrackOrderResponse> trackOrder(
            @PathVariable String orderCode
    ) {
        TrackOrderResponse response = orderService.trackOrder(orderCode);
        return ResponseEntity.ok(response);
    }
}
