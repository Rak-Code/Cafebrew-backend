package com.rakeshgupta.cafebrew_backend.customer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemResponse {
    
    private Long id;
    private String name;
    private BigDecimal price;
    private String category;
    private Boolean available;
}