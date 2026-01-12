package com.rakeshgupta.cafebrew_backend.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminOrderItemResponse {
    private Long id;
    private String menuItemName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalPrice;
    private List<OrderItemExtraResponse> extras = new ArrayList<>();

    /**
     * Constructor without extras for backward compatibility.
     */
    public AdminOrderItemResponse(Long id, String menuItemName, BigDecimal price, Integer quantity, BigDecimal totalPrice) {
        this.id = id;
        this.menuItemName = menuItemName;
        this.price = price;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.extras = new ArrayList<>();
    }
}
