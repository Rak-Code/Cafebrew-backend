package com.rakeshgupta.cafebrew_backend.customer.repository;

import com.rakeshgupta.cafebrew_backend.customer.entity.Category;
import com.rakeshgupta.cafebrew_backend.customer.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    
    /**
     * @deprecated Use findByAvailableTrueAndCategoryEntityActiveTrueOrderByCategoryEntityDisplayOrderAscNameAsc instead.
     * Fetch available menu items sorted by category and name for customer display
     */
    @Deprecated
    List<MenuItem> findByAvailableTrueOrderByCategoryAscNameAsc();
    
    /**
     * @deprecated Use findAllByOrderByCategoryEntityDisplayOrderAscNameAsc instead.
     * Fetch ALL menu items sorted by category and name for admin management
     */
    @Deprecated
    List<MenuItem> findAllByOrderByCategoryAscNameAsc();
    
    /**
     * Find menu item by ID only if it's available (prevents ordering unavailable items)
     */
    Optional<MenuItem> findByIdAndAvailableTrue(Long id);
    
    /**
     * @deprecated Use countByCategoryEntity instead.
     * Count menu items by category name (for checking if category can be deleted)
     */
    @Deprecated
    int countByCategory(String category);
    
    // ==================== New methods using Category entity ====================
    
    /**
     * Fetch available menu items with active categories, sorted by category display order and name.
     * This is the primary method for customer-facing menu display.
     */
    @Query("SELECT m FROM MenuItem m JOIN FETCH m.categoryEntity c " +
           "WHERE m.available = true AND c.active = true " +
           "ORDER BY c.displayOrder ASC, m.name ASC")
    List<MenuItem> findAvailableMenuItemsWithActiveCategories();
    
    /**
     * Fetch ALL menu items with their categories, sorted by category display order and name.
     * This is for admin management where all items should be visible.
     */
    @Query("SELECT m FROM MenuItem m LEFT JOIN FETCH m.categoryEntity c " +
           "ORDER BY COALESCE(c.displayOrder, 999999) ASC, m.name ASC")
    List<MenuItem> findAllMenuItemsWithCategories();
    
    /**
     * Count menu items by Category entity (for checking if category can be deleted)
     */
    int countByCategoryEntity(Category category);
    
    /**
     * Count menu items by Category ID (for checking if category can be deleted)
     */
    @Query("SELECT COUNT(m) FROM MenuItem m WHERE m.categoryEntity.id = :categoryId")
    int countByCategoryEntityId(@Param("categoryId") Long categoryId);
    
    /**
     * Find all menu items by Category entity
     */
    List<MenuItem> findByCategoryEntity(Category category);
    
    /**
     * Find all menu items by Category ID
     */
    List<MenuItem> findByCategoryEntityId(Long categoryId);
    
    /**
     * Find available menu items by Category ID
     */
    List<MenuItem> findByCategoryEntityIdAndAvailableTrue(Long categoryId);
    
    /**
     * Find menu item by ID with category eagerly loaded
     */
    @Query("SELECT m FROM MenuItem m LEFT JOIN FETCH m.categoryEntity WHERE m.id = :id")
    Optional<MenuItem> findByIdWithCategory(@Param("id") Long id);
    
    /**
     * Find available menu item by ID with category eagerly loaded
     * (prevents ordering unavailable items and ensures category is loaded)
     */
    @Query("SELECT m FROM MenuItem m JOIN FETCH m.categoryEntity c " +
           "WHERE m.id = :id AND m.available = true AND c.active = true")
    Optional<MenuItem> findAvailableByIdWithActiveCategory(@Param("id") Long id);
}