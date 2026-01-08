package com.rakeshgupta.cafebrew_backend.customer.repository;

import com.rakeshgupta.cafebrew_backend.customer.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    
    /**
     * Fetch available menu items sorted by category and name for customer display
     */
    List<MenuItem> findByAvailableTrueOrderByCategoryAscNameAsc();
    
    /**
     * Fetch ALL menu items sorted by category and name for admin management
     */
    List<MenuItem> findAllByOrderByCategoryAscNameAsc();
    
    /**
     * Find menu item by ID only if it's available (prevents ordering unavailable items)
     */
    Optional<MenuItem> findByIdAndAvailableTrue(Long id);
}