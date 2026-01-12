package com.rakeshgupta.cafebrew_backend.customer.service;

import com.rakeshgupta.cafebrew_backend.admin.dto.request.CreateExtraIngredientRequest;
import com.rakeshgupta.cafebrew_backend.admin.dto.request.UpdateExtraIngredientRequest;
import com.rakeshgupta.cafebrew_backend.customer.entity.ExtraIngredient;

import java.util.List;

/**
 * Service interface for managing extra ingredients.
 * Provides operations for both admin management and customer queries.
 */
public interface ExtraIngredientService {

    // ==================== Admin Operations ====================

    /**
     * Get all extra ingredients sorted by name for admin management.
     * Returns all ingredients regardless of active status.
     *
     * @return list of all extra ingredients with their category mappings
     */
    List<ExtraIngredient> getAllExtraIngredients();

    /**
     * Get an extra ingredient by its ID.
     *
     * @param id the extra ingredient ID
     * @return the extra ingredient
     * @throws com.rakeshgupta.cafebrew_backend.common.exception.ExtraIngredientNotFoundException if not found
     */
    ExtraIngredient getExtraIngredientById(Long id);

    /**
     * Create a new extra ingredient.
     * Validates name uniqueness, length limits, and price positivity.
     *
     * @param request the creation request containing name, description, price, and category IDs
     * @return the created extra ingredient
     * @throws com.rakeshgupta.cafebrew_backend.common.exception.DuplicateExtraIngredientNameException if name exists
     * @throws com.rakeshgupta.cafebrew_backend.common.exception.InvalidCategoryException if any category ID is invalid
     */
    ExtraIngredient createExtraIngredient(CreateExtraIngredientRequest request);

    /**
     * Update an existing extra ingredient.
     * Validates name uniqueness (excluding current), length limits, and price positivity.
     * Handles category mapping updates (add/remove mappings).
     *
     * @param id the extra ingredient ID to update
     * @param request the update request containing name, description, price, active status, and category IDs
     * @return the updated extra ingredient
     * @throws com.rakeshgupta.cafebrew_backend.common.exception.ExtraIngredientNotFoundException if not found
     * @throws com.rakeshgupta.cafebrew_backend.common.exception.DuplicateExtraIngredientNameException if name exists for another ingredient
     * @throws com.rakeshgupta.cafebrew_backend.common.exception.InvalidCategoryException if any category ID is invalid
     */
    ExtraIngredient updateExtraIngredient(Long id, UpdateExtraIngredientRequest request);

    /**
     * Delete an extra ingredient by ID.
     * Prevents deletion if the ingredient has order history.
     *
     * @param id the extra ingredient ID to delete
     * @throws com.rakeshgupta.cafebrew_backend.common.exception.ExtraIngredientNotFoundException if not found
     * @throws com.rakeshgupta.cafebrew_backend.common.exception.ExtraIngredientHasOrdersException if has order history
     */
    void deleteExtraIngredient(Long id);

    // ==================== Customer Operations ====================

    /**
     * Get active extra ingredients for a specific category.
     * Only returns ingredients where both the ingredient and category are active.
     *
     * @param categoryId the category ID
     * @return list of active extra ingredients for the category
     */
    List<ExtraIngredient> getActiveExtraIngredientsByCategory(Long categoryId);

    /**
     * Get active extra ingredients for multiple categories.
     * Only returns ingredients where both the ingredient and category are active.
     *
     * @param categoryIds list of category IDs
     * @return list of active extra ingredients for the categories
     */
    List<ExtraIngredient> getActiveExtraIngredientsByCategories(List<Long> categoryIds);

    // ==================== Order Validation ====================

    /**
     * Validate that all extra ingredient IDs are valid and active.
     * Used during order placement to ensure selected extras are still available.
     *
     * @param extraIngredientIds list of extra ingredient IDs to validate
     * @throws com.rakeshgupta.cafebrew_backend.common.exception.ExtraIngredientNotFoundException if any ID is not found
     * @throws com.rakeshgupta.cafebrew_backend.common.exception.InactiveExtraIngredientException if any ingredient is inactive
     */
    void validateExtraIngredients(List<Long> extraIngredientIds);

    /**
     * Get extra ingredients by their IDs.
     * Used during order creation to fetch ingredient details.
     *
     * @param ids list of extra ingredient IDs
     * @return list of extra ingredients
     * @throws com.rakeshgupta.cafebrew_backend.common.exception.ExtraIngredientNotFoundException if any ID is not found
     */
    List<ExtraIngredient> getExtraIngredientsByIds(List<Long> ids);
}
