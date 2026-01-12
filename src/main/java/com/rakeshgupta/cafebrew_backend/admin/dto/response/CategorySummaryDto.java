package com.rakeshgupta.cafebrew_backend.admin.dto.response;

import com.rakeshgupta.cafebrew_backend.customer.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight DTO for category information when nested in other responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategorySummaryDto {

    private Long id;
    private String name;

    /**
     * Creates a CategorySummaryDto from a Category entity.
     */
    public static CategorySummaryDto fromEntity(Category category) {
        return new CategorySummaryDto(
            category.getId(),
            category.getName()
        );
    }
}
