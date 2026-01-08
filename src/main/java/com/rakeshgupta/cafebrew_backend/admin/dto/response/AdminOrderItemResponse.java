package com.rakeshgupta.cafebrew_backend.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminOrderItemResponse {
    private Long id;
    private String menuItemName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalPrice;
}
