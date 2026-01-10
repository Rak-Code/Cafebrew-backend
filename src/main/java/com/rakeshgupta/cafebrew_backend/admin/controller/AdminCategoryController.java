package com.rakeshgupta.cafebrew_backend.admin.controller;

import com.rakeshgupta.cafebrew_backend.admin.dto.request.CategoryOrderRequest;
import com.rakeshgupta.cafebrew_backend.admin.dto.request.CreateCategoryRequest;
import com.rakeshgupta.cafebrew_backend.admin.dto.request.UpdateCategoryRequest;
import com.rakeshgupta.cafebrew_backend.admin.dto.response.CategoryResponse;
import com.rakeshgupta.cafebrew_backend.customer.entity.Category;
import com.rakeshgupta.cafebrew_backend.customer.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    /**
     * GET /api/admin/categories
     * Returns all categories (including inactive) for admin management.
     * Requires authentication with ADMIN, OWNER, or STAFF role.
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        List<CategoryResponse> response = categories.stream()
                .map(category -> CategoryResponse.fromEntity(
                        category,
                        categoryService.getItemCountForCategory(category.getId())
                ))
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/admin/categories
     * Creates a new category.
     * Requires authentication with ADMIN, OWNER, or STAFF role.
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CreateCategoryRequest request
    ) {
        Category category = categoryService.createCategory(request);
        CategoryResponse response = CategoryResponse.fromEntity(category, 0);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/admin/categories/{id}
     * Updates an existing category.
     * Requires authentication with ADMIN, OWNER, or STAFF role.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        Category category = categoryService.updateCategory(id, request);
        CategoryResponse response = CategoryResponse.fromEntity(
                category,
                categoryService.getItemCountForCategory(category.getId())
        );
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/admin/categories/{id}
     * Deletes a category if it has no associated menu items.
     * Requires authentication with ADMIN, OWNER, or STAFF role.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * PUT /api/admin/categories/reorder
     * Reorders categories by updating their displayOrder values.
     * Requires authentication with ADMIN, OWNER, or STAFF role.
     */
    @PutMapping("/reorder")
    public ResponseEntity<Void> reorderCategories(
            @Valid @RequestBody List<CategoryOrderRequest> orderRequests
    ) {
        categoryService.reorderCategories(orderRequests);
        return ResponseEntity.ok().build();
    }
}
