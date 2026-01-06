package com.rakeshgupta.cafebrew_backend.admin.dto.response;

import com.rakeshgupta.cafebrew_backend.common.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminOrderResponse {
    
    private Long orderId;
    private String orderCode;
    private String customerName;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
}