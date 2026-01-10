package com.rakeshgupta.cafebrew_backend.customer.service;

import com.rakeshgupta.cafebrew_backend.admin.dto.request.CreateMenuItemRequest;
import com.rakeshgupta.cafebrew_backend.admin.dto.request.UpdateMenuItemRequest;
import com.rakeshgupta.cafebrew_backend.common.exception.CategoryNotFoundException;
import com.rakeshgupta.cafebrew_backend.common.exception.MenuItemNotFoundException;
import com.rakeshgupta.cafebrew_backend.customer.entity.Category;
import com.rakeshgupta.cafebrew_backend.customer.entity.MenuItem;
import com.rakeshgupta.cafebrew_backend.customer.repository.CategoryRepository;
import com.rakeshgupta.cafebrew_backend.customer.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {
    
    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;
    
    /**
     * Get available menu items for customers.
     * Returns only available items with active categories, sorted by category display order and name.
     * This filters out items whose category has been deactivated.
     */
    @Transactional(readOnly = true)
    public List<MenuItem> getAvailableMenu() {
        return menuItemRepository.findAvailableMenuItemsWithActiveCategories();
    }
    
    /**
     * Get ALL menu items for admin management.
     * Returns all items (including unavailable and those in inactive categories) 
     * sorted by category display order and name.
     */
    @Transactional(readOnly = true)
    public List<MenuItem> getAllMenuItems() {
        return menuItemRepository.findAllMenuItemsWithCategories();
    }
    
    /**
     * Toggle menu item availability (Admin only).
     * Updates availability status for menu management.
     */
    @Transactional
    public void updateAvailability(Long menuItemId, boolean available) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new MenuItemNotFoundException(menuItemId));
        
        menuItem.setAvailable(available);
        menuItemRepository.save(menuItem);
    }
    
    /**
     * Get a single menu item by ID with category loaded.
     * @param id the menu item ID
     * @return the menu item with category
     * @throws MenuItemNotFoundException if menu item not found
     */
    @Transactional(readOnly = true)
    public MenuItem getMenuItemById(Long id) {
        return menuItemRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new MenuItemNotFoundException(id));
    }
    
    /**
     * Create a new menu item.
     * Validates that the category exists and is active.
     * @param request the create request with menu item data
     * @return the created menu item
     * @throws CategoryNotFoundException if category not found
     * @throws IllegalArgumentException if category is inactive
     */
    @Transactional
    public MenuItem createMenuItem(CreateMenuItemRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException(request.getCategoryId()));
        
        if (!category.getActive()) {
            throw new IllegalArgumentException("Cannot create menu item in inactive category");
        }
        
        MenuItem menuItem = new MenuItem();
        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setCategoryEntity(category);
        menuItem.setCategory(category.getName()); // Keep legacy field in sync
        menuItem.setPrice(request.getPrice());
        menuItem.setAvailable(request.getAvailable() != null ? request.getAvailable() : true);
        menuItem.setImageUrl(request.getImageUrl());
        
        return menuItemRepository.save(menuItem);
    }
    
    /**
     * Update an existing menu item.
     * Validates that the category exists and is active.
     * @param id the menu item ID to update
     * @param request the update request with new data
     * @return the updated menu item
     * @throws MenuItemNotFoundException if menu item not found
     * @throws CategoryNotFoundException if category not found
     * @throws IllegalArgumentException if category is inactive
     */
    @Transactional
    public MenuItem updateMenuItem(Long id, UpdateMenuItemRequest request) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new MenuItemNotFoundException(id));
        
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException(request.getCategoryId()));
        
        if (!category.getActive()) {
            throw new IllegalArgumentException("Cannot assign menu item to inactive category");
        }
        
        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setCategoryEntity(category);
        menuItem.setCategory(category.getName()); // Keep legacy field in sync
        menuItem.setPrice(request.getPrice());
        if (request.getAvailable() != null) {
            menuItem.setAvailable(request.getAvailable());
        }
        menuItem.setImageUrl(request.getImageUrl());
        
        return menuItemRepository.save(menuItem);
    }
    
    /**
     * Delete a menu item by ID.
     * @param id the menu item ID to delete
     * @throws MenuItemNotFoundException if menu item not found
     */
    @Transactional
    public void deleteMenuItem(Long id) {
        if (!menuItemRepository.existsById(id)) {
            throw new MenuItemNotFoundException(id);
        }
        menuItemRepository.deleteById(id);
    }
}