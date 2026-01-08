package com.rakeshgupta.cafebrew_backend.customer.service;

import com.rakeshgupta.cafebrew_backend.customer.entity.MenuItem;
import com.rakeshgupta.cafebrew_backend.customer.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {
    
    private final MenuItemRepository menuItemRepository;
    
    /**
     * Get available menu items for customers
     * Returns only available items sorted by category and name
     */
    @Transactional(readOnly = true)
    public List<MenuItem> getAvailableMenu() {
        return menuItemRepository.findByAvailableTrueOrderByCategoryAscNameAsc();
    }
    
    /**
     * Get ALL menu items for admin management
     * Returns all items (including unavailable) sorted by category and name
     */
    @Transactional(readOnly = true)
    public List<MenuItem> getAllMenuItems() {
        return menuItemRepository.findAllByOrderByCategoryAscNameAsc();
    }
    
    /**
     * Toggle menu item availability (Admin only)
     * Updates availability status for menu management
     */
    @Transactional
    public void updateAvailability(Long menuItemId, boolean available) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));
        
        menuItem.setAvailable(available);
        menuItemRepository.save(menuItem);
    }
}