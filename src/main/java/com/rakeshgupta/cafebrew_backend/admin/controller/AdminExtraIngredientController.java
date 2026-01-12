package com.rakeshgupta.cafebrew_backend.admin.controller;

import com.rakeshgupta.cafebrew_backend.admin.dto.request.CreateExtraIngredientRequest;
import com.rakeshgupta.cafebrew_backend.admin.dto.request.UpdateExtraIngredientRequest;
import com.rakeshgupta.cafebrew_backend.admin.dto.response.ExtraIngredientResponse;
import com.rakeshgupta.cafebrew_backend.customer.entity.ExtraIngredient;
import com.rakeshgupta.cafebrew_backend.customer.service.ExtraIngredientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin controller for managing extra ingredients.
 * All endpoints require authentication with ADMIN, OWNER, or STAFF role.
 * Authentication is enforced by SecurityConfig for /api/admin/** paths.
 */
@RestController
@RequestMapping("/api/admin/extra-ingredients")
@RequiredArgsConstructor
public class AdminExtraIngredientController {

    private final ExtraIngredientService extraIngredientService;

    /**
     * GET /api/admin/extra-ingredients
     * Returns all extra ingredients (including inactive) for admin management.
     * Requires authentication with ADMIN, OWNER, or STAFF role.
     */
    @GetMapping
    public ResponseEntity<List<ExtraIngredientResponse>> getAllExtraIngredients() {
        List<ExtraIngredient> extraIngredients = extraIngredientService.getAllExtraIngredients();
        List<ExtraIngredientResponse> response = extraIngredients.stream()
                .map(ExtraIngredientResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/admin/extra-ingredients
     * Creates a new extra ingredient.
     * Requires authentication with ADMIN, OWNER, or STAFF role.
     */
    @PostMapping
    public ResponseEntity<ExtraIngredientResponse> createExtraIngredient(
            @Valid @RequestBody CreateExtraIngredientRequest request
    ) {
        ExtraIngredient extraIngredient = extraIngredientService.createExtraIngredient(request);
        ExtraIngredientResponse response = ExtraIngredientResponse.fromEntity(extraIngredient);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/admin/extra-ingredients/{id}
     * Updates an existing extra ingredient.
     * Requires authentication with ADMIN, OWNER, or STAFF role.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ExtraIngredientResponse> updateExtraIngredient(
            @PathVariable Long id,
            @Valid @RequestBody UpdateExtraIngredientRequest request
    ) {
        ExtraIngredient extraIngredient = extraIngredientService.updateExtraIngredient(id, request);
        ExtraIngredientResponse response = ExtraIngredientResponse.fromEntity(extraIngredient);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/admin/extra-ingredients/{id}
     * Deletes an extra ingredient if it has no order history.
     * Requires authentication with ADMIN, OWNER, or STAFF role.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExtraIngredient(@PathVariable Long id) {
        extraIngredientService.deleteExtraIngredient(id);
        return ResponseEntity.noContent().build();
    }
}
