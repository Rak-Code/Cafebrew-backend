package com.rakeshgupta.cafebrew_backend.customer.dto.response;

import com.rakeshgupta.cafebrew_backend.common.enums.OrderStatus;
import com.rakeshgupta.cafebrew_backend.common.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderResponse {
    
    private String orderCode;
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
    private BigDecimal totalAmount;
    private String razorpayOrderId;
    private String message;
    
    public PlaceOrderResponse(String orderCode, OrderStatus orderStatus, PaymentStatus paymentStatus, 
                             BigDecimal totalAmount, String message) {
        this.orderCode = orderCode;
        this.orderStatus = orderStatus;
        this.paymentStatus = paymentStatus;
        this.totalAmount = totalAmount;
        this.message = message;
    }
}