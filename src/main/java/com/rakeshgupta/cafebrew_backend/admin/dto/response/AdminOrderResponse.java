package com.rakeshgupta.cafebrew_backend.admin.dto.response;

import com.rakeshgupta.cafebrew_backend.common.enums.OrderStatus;
import com.rakeshgupta.cafebrew_backend.common.enums.PaymentMode;
import com.rakeshgupta.cafebrew_backend.common.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminOrderResponse {
    
    private Long orderId;
    private String orderCode;
    private String customerName;
    private String customerPhone;
    private String tableNo;
    private OrderStatus status;
    private PaymentMode paymentMode;
    private PaymentStatus paymentStatus;
    private BigDecimal totalAmount;
    private List<AdminOrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}