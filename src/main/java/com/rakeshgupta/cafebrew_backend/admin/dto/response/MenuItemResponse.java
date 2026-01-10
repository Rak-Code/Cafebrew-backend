package com.rakeshgupta.cafebrew_backend.admin.dto.response;

import com.rakeshgupta.cafebrew_backend.customer.entity.MenuItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemResponse {
    
    private Long id;
    private String name;
    private String description;
    private Long categoryId;
    private String categoryName;
    private BigDecimal price;
    private Boolean available;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Creates a MenuItemResponse from a MenuItem entity.
     * Includes categoryId and categoryName for display purposes.
     */
    public static MenuItemResponse fromEntity(MenuItem menuItem) {
        return new MenuItemResponse(
            menuItem.getId(),
            menuItem.getName(),
            menuItem.getDescription(),
            menuItem.getCategoryId(),
            menuItem.getCategoryName(),
            menuItem.getPrice(),
            menuItem.getAvailable(),
            menuItem.getImageUrl(),
            menuItem.getCreatedAt(),
            menuItem.getUpdatedAt()
        );
    }
}
