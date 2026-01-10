package com.rakeshgupta.cafebrew_backend.admin.dto.response;

import com.rakeshgupta.cafebrew_backend.customer.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    
    private Long id;
    private String name;
    private String description;
    private Integer displayOrder;
    private Boolean active;
    private Integer itemCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Creates a CategoryResponse from a Category entity.
     * Item count defaults to 0 and should be set separately if needed.
     */
    public static CategoryResponse fromEntity(Category category) {
        return new CategoryResponse(
            category.getId(),
            category.getName(),
            category.getDescription(),
            category.getDisplayOrder(),
            category.getActive(),
            0, // itemCount - to be set separately
            category.getCreatedAt(),
            category.getUpdatedAt()
        );
    }
    
    /**
     * Creates a CategoryResponse from a Category entity with item count.
     */
    public static CategoryResponse fromEntity(Category category, Integer itemCount) {
        return new CategoryResponse(
            category.getId(),
            category.getName(),
            category.getDescription(),
            category.getDisplayOrder(),
            category.getActive(),
            itemCount,
            category.getCreatedAt(),
            category.getUpdatedAt()
        );
    }
}
