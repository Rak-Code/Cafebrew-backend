package com.rakeshgupta.cafebrew_backend.customer.service;

import com.rakeshgupta.cafebrew_backend.admin.dto.request.CreateExtraIngredientRequest;
import com.rakeshgupta.cafebrew_backend.admin.dto.request.UpdateExtraIngredientRequest;
import com.rakeshgupta.cafebrew_backend.common.exception.*;
import com.rakeshgupta.cafebrew_backend.customer.entity.Category;
import com.rakeshgupta.cafebrew_backend.customer.entity.ExtraIngredient;
import com.rakeshgupta.cafebrew_backend.customer.repository.CategoryRepository;
import com.rakeshgupta.cafebrew_backend.customer.repository.ExtraIngredientRepository;
import com.rakeshgupta.cafebrew_backend.customer.repository.OrderItemExtraRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of ExtraIngredientService.
 * Handles CRUD operations for extra ingredients with category mapping management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExtraIngredientServiceImpl implements ExtraIngredientService {

    private final ExtraIngredientRepository extraIngredientRepository;
    private final CategoryRepository categoryRepository;
    private final OrderItemExtraRepository orderItemExtraRepository;

    // ==================== Admin Operations ====================

    @Override
    @Transactional(readOnly = true)
    public List<ExtraIngredient> getAllExtraIngredients() {
        return extraIngredientRepository.findAllWithCategories();
    }

    @Override
    @Transactional(readOnly = true)
    public ExtraIngredient getExtraIngredientById(Long id) {
        return extraIngredientRepository.findById(id)
                .orElseThrow(() -> new ExtraIngredientNotFoundException(id));
    }

    @Override
    @Transactional
    public ExtraIngredient createExtraIngredient(CreateExtraIngredientRequest request) {
        // Validate unique name
        if (extraIngredientRepository.existsByName(request.getName())) {
            throw new DuplicateExtraIngredientNameException(request.getName());
        }

        // Create the extra ingredient
        ExtraIngredient extraIngredient = new ExtraIngredient();
        extraIngredient.setName(request.getName());
        extraIngredient.setDescription(request.getDescription());
        extraIngredient.setPrice(request.getPrice());
        extraIngredient.setActive(true);

        // Save first to get the ID
        extraIngredient = extraIngredientRepository.save(extraIngredient);

        // Add category mappings if provided
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            Set<Category> categories = fetchAndValidateCategories(request.getCategoryIds());
            for (Category category : categories) {
                extraIngredient.addCategory(category);
            }
            extraIngredient = extraIngredientRepository.save(extraIngredient);
        }

        log.info("Created extra ingredient: {} with {} categories", 
                extraIngredient.getName(), 
                extraIngredient.getCategories().size());

        return extraIngredient;
    }

    @Override
    @Transactional
    public ExtraIngredient updateExtraIngredient(Long id, UpdateExtraIngredientRequest request) {
        ExtraIngredient extraIngredient = extraIngredientRepository.findById(id)
                .orElseThrow(() -> new ExtraIngredientNotFoundException(id));

        // Validate unique name (excluding current ingredient)
        if (extraIngredientRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new DuplicateExtraIngredientNameException(request.getName());
        }

        // Update basic fields
        extraIngredient.setName(request.getName());
        extraIngredient.setDescription(request.getDescription());
        extraIngredient.setPrice(request.getPrice());

        if (request.getActive() != null) {
            extraIngredient.setActive(request.getActive());
        }

        // Update category mappings
        updateCategoryMappings(extraIngredient, request.getCategoryIds());

        extraIngredient = extraIngredientRepository.save(extraIngredient);

        log.info("Updated extra ingredient: {} with {} categories", 
                extraIngredient.getName(), 
                extraIngredient.getCategories().size());

        return extraIngredient;
    }

    @Override
    @Transactional
    public void deleteExtraIngredient(Long id) {
        ExtraIngredient extraIngredient = extraIngredientRepository.findById(id)
                .orElseThrow(() -> new ExtraIngredientNotFoundException(id));

        // Check for order history
        long orderCount = orderItemExtraRepository.countByExtraIngredientId(id);
        if (orderCount > 0) {
            throw new ExtraIngredientHasOrdersException(extraIngredient.getName(), orderCount);
        }

        // Clear category mappings before deletion
        extraIngredient.clearCategories();
        extraIngredientRepository.delete(extraIngredient);

        log.info("Deleted extra ingredient: {}", extraIngredient.getName());
    }

    // ==================== Customer Operations ====================

    @Override
    @Transactional(readOnly = true)
    public List<ExtraIngredient> getActiveExtraIngredientsByCategory(Long categoryId) {
        return extraIngredientRepository.findActiveByCategory(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExtraIngredient> getActiveExtraIngredientsByCategories(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Collections.emptyList();
        }
        return extraIngredientRepository.findActiveByCategories(categoryIds);
    }

    // ==================== Order Validation ====================

    @Override
    @Transactional(readOnly = true)
    public void validateExtraIngredients(List<Long> extraIngredientIds) {
        if (extraIngredientIds == null || extraIngredientIds.isEmpty()) {
            return;
        }

        List<Long> inactiveIds = new ArrayList<>();
        List<Long> notFoundIds = new ArrayList<>();

        for (Long id : extraIngredientIds) {
            Optional<ExtraIngredient> optionalIngredient = extraIngredientRepository.findById(id);
            if (optionalIngredient.isEmpty()) {
                notFoundIds.add(id);
            } else if (!optionalIngredient.get().getActive()) {
                inactiveIds.add(id);
            }
        }

        if (!notFoundIds.isEmpty()) {
            throw new ExtraIngredientNotFoundException(notFoundIds.get(0));
        }

        if (!inactiveIds.isEmpty()) {
            throw new InactiveExtraIngredientException(
                    "One or more selected extras are no longer available: " + inactiveIds);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExtraIngredient> getExtraIngredientsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        List<ExtraIngredient> ingredients = extraIngredientRepository.findAllById(ids);
        
        // Verify all IDs were found
        if (ingredients.size() != ids.size()) {
            Set<Long> foundIds = ingredients.stream()
                    .map(ExtraIngredient::getId)
                    .collect(Collectors.toSet());
            
            for (Long id : ids) {
                if (!foundIds.contains(id)) {
                    throw new ExtraIngredientNotFoundException(id);
                }
            }
        }

        return ingredients;
    }

    // ==================== Private Helper Methods ====================

    /**
     * Fetches and validates category IDs.
     * Throws InvalidCategoryException if any category ID is not found.
     */
    private Set<Category> fetchAndValidateCategories(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return new HashSet<>();
        }

        Set<Category> categories = new HashSet<>();
        for (Long categoryId : categoryIds) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new InvalidCategoryException(categoryId));
            categories.add(category);
        }

        return categories;
    }

    /**
     * Updates category mappings for an extra ingredient.
     * Adds new mappings and removes old ones to match the requested category IDs.
     */
    private void updateCategoryMappings(ExtraIngredient extraIngredient, List<Long> newCategoryIds) {
        Set<Long> newIds = newCategoryIds != null ? new HashSet<>(newCategoryIds) : new HashSet<>();
        
        // Get current category IDs
        Set<Long> currentIds = extraIngredient.getCategories().stream()
                .map(Category::getId)
                .collect(Collectors.toSet());

        // Find categories to remove
        Set<Long> toRemove = new HashSet<>(currentIds);
        toRemove.removeAll(newIds);

        // Find categories to add
        Set<Long> toAdd = new HashSet<>(newIds);
        toAdd.removeAll(currentIds);

        // Remove old mappings
        for (Long categoryId : toRemove) {
            Category category = categoryRepository.findById(categoryId).orElse(null);
            if (category != null) {
                extraIngredient.removeCategory(category);
            }
        }

        // Add new mappings
        for (Long categoryId : toAdd) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new InvalidCategoryException(categoryId));
            extraIngredient.addCategory(category);
        }
    }
}
