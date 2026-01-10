package com.rakeshgupta.cafebrew_backend.customer.controller;

import com.rakeshgupta.cafebrew_backend.admin.dto.response.CategoryResponse;
import com.rakeshgupta.cafebrew_backend.customer.entity.Category;
import com.rakeshgupta.cafebrew_backend.customer.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * GET /api/categories
     * Public endpoint - returns only active categories for customer display.
     * Categories are sorted by displayOrder.
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getActiveCategories() {
        List<Category> categories = categoryService.getActiveCategories();
        List<CategoryResponse> response = categories.stream()
                .map(category -> CategoryResponse.fromEntity(
                        category,
                        categoryService.getItemCountForCategory(category.getId())
                ))
                .toList();
        return ResponseEntity.ok(response);
    }
}
