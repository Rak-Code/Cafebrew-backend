package com.rakeshgupta.cafebrew_backend.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryOrderRequest {
    
    @NotNull(message = "Category ID is required")
    private Long id;
    
    @NotNull(message = "Display order is required")
    private Integer displayOrder;
}
