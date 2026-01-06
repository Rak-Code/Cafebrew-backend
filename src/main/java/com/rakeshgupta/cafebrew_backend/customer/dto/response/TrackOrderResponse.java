package com.rakeshgupta.cafebrew_backend.customer.dto.response;

import com.rakeshgupta.cafebrew_backend.common.enums.OrderStatus;
import com.rakeshgupta.cafebrew_backend.common.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackOrderResponse {
    
    private String orderCode;
    private String customerName;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private List<OrderItemResponse> items;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        
        private String name;
        private Integer quantity;
    }
}