package com.rakeshgupta.cafebrew_backend.customer.controller;

import com.rakeshgupta.cafebrew_backend.admin.dto.response.ExtraIngredientResponse;
import com.rakeshgupta.cafebrew_backend.customer.entity.ExtraIngredient;
import com.rakeshgupta.cafebrew_backend.customer.service.ExtraIngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public controller for customer access to extra ingredients.
 * Returns only active ingredients for active categories.
 */
@RestController
@RequestMapping("/api/extra-ingredients")
@RequiredArgsConstructor
public class ExtraIngredientController {

    private final ExtraIngredientService extraIngredientService;

    /**
     * GET /api/extra-ingredients/by-category/{categoryId}
     * Public endpoint - returns only active extra ingredients for the specified category.
     * Only returns ingredients where both the ingredient and category are active.
     */
    @GetMapping("/by-category/{categoryId}")
    public ResponseEntity<List<ExtraIngredientResponse>> getExtraIngredientsByCategory(
            @PathVariable Long categoryId
    ) {
        List<ExtraIngredient> extraIngredients = extraIngredientService.getActiveExtraIngredientsByCategory(categoryId);
        List<ExtraIngredientResponse> response = extraIngredients.stream()
                .map(ExtraIngredientResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/extra-ingredients/by-categories?ids=1,2,3
     * Public endpoint - returns only active extra ingredients for the specified categories.
     * Only returns ingredients where both the ingredient and category are active.
     */
    @GetMapping("/by-categories")
    public ResponseEntity<List<ExtraIngredientResponse>> getExtraIngredientsByCategories(
            @RequestParam("ids") List<Long> categoryIds
    ) {
        List<ExtraIngredient> extraIngredients = extraIngredientService.getActiveExtraIngredientsByCategories(categoryIds);
        List<ExtraIngredientResponse> response = extraIngredients.stream()
                .map(ExtraIngredientResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }
}
