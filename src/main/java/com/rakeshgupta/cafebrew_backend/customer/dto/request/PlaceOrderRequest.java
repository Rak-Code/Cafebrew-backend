package com.rakeshgupta.cafebrew_backend.customer.dto.request;

import com.rakeshgupta.cafebrew_backend.common.enums.PaymentMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderRequest {
    
    @NotBlank(message = "Customer name is required")
    @Size(max = 50, message = "Customer name must not exceed 50 characters")
    private String customerName;
    
    @NotBlank(message = "Phone number is required")
    @Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 characters")
    private String customerPhone;
    
    @NotNull(message = "Payment mode is required")
    private PaymentMode paymentMode;
    
    @Size(max = 20, message = "Table number must not exceed 20 characters")
    private String tableNo;
    
    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemRequest> items;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        
        @NotNull(message = "Menu item ID is required")
        private Long menuItemId;
        
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;

        /**
         * Optional list of extra ingredient IDs to add to this order item.
         */
        private List<Long> extraIngredientIds;
    }
}