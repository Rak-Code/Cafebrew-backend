package com.rakeshgupta.cafebrew_backend.admin.dto.response;

import com.rakeshgupta.cafebrew_backend.customer.entity.ExtraIngredient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Response DTO for extra ingredient data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtraIngredientResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Boolean active;
    private List<CategorySummaryDto> categories;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Creates an ExtraIngredientResponse from an ExtraIngredient entity.
     */
    public static ExtraIngredientResponse fromEntity(ExtraIngredient extraIngredient) {
        List<CategorySummaryDto> categorySummaries = extraIngredient.getCategories()
            .stream()
            .map(CategorySummaryDto::fromEntity)
            .collect(Collectors.toList());

        return new ExtraIngredientResponse(
            extraIngredient.getId(),
            extraIngredient.getName(),
            extraIngredient.getDescription(),
            extraIngredient.getPrice(),
            extraIngredient.getActive(),
            categorySummaries,
            extraIngredient.getCreatedAt(),
            extraIngredient.getUpdatedAt()
        );
    }
}
