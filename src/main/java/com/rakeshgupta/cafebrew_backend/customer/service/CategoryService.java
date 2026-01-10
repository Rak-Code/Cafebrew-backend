package com.rakeshgupta.cafebrew_backend.customer.service;

import com.rakeshgupta.cafebrew_backend.admin.dto.request.CategoryOrderRequest;
import com.rakeshgupta.cafebrew_backend.admin.dto.request.CreateCategoryRequest;
import com.rakeshgupta.cafebrew_backend.admin.dto.request.UpdateCategoryRequest;
import com.rakeshgupta.cafebrew_backend.common.exception.CategoryHasItemsException;
import com.rakeshgupta.cafebrew_backend.common.exception.CategoryNotFoundException;
import com.rakeshgupta.cafebrew_backend.common.exception.DuplicateCategoryNameException;
import com.rakeshgupta.cafebrew_backend.customer.entity.Category;
import com.rakeshgupta.cafebrew_backend.customer.repository.CategoryRepository;
import com.rakeshgupta.cafebrew_backend.customer.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;
    
    /**
     * Get all categories sorted by displayOrder for admin management.
     * Returns all categories regardless of active status.
     */
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderByDisplayOrderAsc();
    }
    
    /**
     * Get only active categories sorted by displayOrder for customer display.
     */
    @Transactional(readOnly = true)
    public List<Category> getActiveCategories() {
        return categoryRepository.findByActiveTrueOrderByDisplayOrderAsc();
    }
    
    /**
     * Get a category by its ID.
     * @throws CategoryNotFoundException if category not found
     */
    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    
    /**
     * Create a new category.
     * Validates that the category name is unique.
     * @throws DuplicateCategoryNameException if name already exists
     */
    @Transactional
    public Category createCategory(CreateCategoryRequest request) {
        // Validate unique name
        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateCategoryNameException(request.getName());
        }
        
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        category.setActive(true);
        
        return categoryRepository.save(category);
    }
    
    /**
     * Update an existing category.
     * Validates that the new name is unique (excluding current category).
     * @throws CategoryNotFoundException if category not found
     * @throws DuplicateCategoryNameException if name already exists for another category
     */
    @Transactional
    public Category updateCategory(Long id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        
        // Validate unique name (excluding current category)
        if (categoryRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new DuplicateCategoryNameException(request.getName());
        }
        
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        
        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }
        
        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }
        
        return categoryRepository.save(category);
    }
    
    /**
     * Delete a category by ID.
     * Prevents deletion if category has associated menu items.
     * @throws CategoryNotFoundException if category not found
     * @throws CategoryHasItemsException if category has menu items
     */
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        
        // Check if category has menu items using the new entity-based method
        int itemCount = menuItemRepository.countByCategoryEntityId(id);
        if (itemCount > 0) {
            throw new CategoryHasItemsException(category.getName(), itemCount);
        }
        
        categoryRepository.delete(category);
    }
    
    /**
     * Reorder categories by updating their displayOrder values.
     * @throws CategoryNotFoundException if any category ID is not found
     */
    @Transactional
    public void reorderCategories(List<CategoryOrderRequest> orderRequests) {
        for (CategoryOrderRequest request : orderRequests) {
            Category category = categoryRepository.findById(request.getId())
                    .orElseThrow(() -> new CategoryNotFoundException(request.getId()));
            category.setDisplayOrder(request.getDisplayOrder());
            categoryRepository.save(category);
        }
    }
    
    /**
     * Get the count of menu items for a given category by ID.
     */
    @Transactional(readOnly = true)
    public int getItemCountForCategory(Long categoryId) {
        return menuItemRepository.countByCategoryEntityId(categoryId);
    }
    
    /**
     * @deprecated Use getItemCountForCategory(Long categoryId) instead.
     * Get the count of menu items for a given category by name.
     */
    @Deprecated
    @Transactional(readOnly = true)
    public int getItemCountForCategoryByName(String categoryName) {
        return menuItemRepository.countByCategory(categoryName);
    }
}
