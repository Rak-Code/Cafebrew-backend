package com.rakeshgupta.cafebrew_backend.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentWebhookRequest {
    
    private String event;
    private PayloadData payload;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PayloadData {
        
        private PaymentData payment;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentData {
        
        private EntityData entity;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EntityData {
        
        @JsonProperty("order_id")
        private String orderId;
        
        private String id;
        private String status;
    }
}