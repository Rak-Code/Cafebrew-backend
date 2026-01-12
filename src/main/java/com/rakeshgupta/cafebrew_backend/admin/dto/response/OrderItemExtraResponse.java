package com.rakeshgupta.cafebrew_backend.admin.dto.response;

import com.rakeshgupta.cafebrew_backend.customer.entity.OrderItemExtra;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for order item extra ingredient data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemExtraResponse {

    private Long id;
    private String extraIngredientName;
    private BigDecimal price;

    /**
     * Creates an OrderItemExtraResponse from an OrderItemExtra entity.
     */
    public static OrderItemExtraResponse fromEntity(OrderItemExtra orderItemExtra) {
        return new OrderItemExtraResponse(
            orderItemExtra.getId(),
            orderItemExtra.getExtraIngredientName(),
            orderItemExtra.getPrice()
        );
    }
}
