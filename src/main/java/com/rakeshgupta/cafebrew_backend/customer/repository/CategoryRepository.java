package com.rakeshgupta.cafebrew_backend.customer.repository;

import com.rakeshgupta.cafebrew_backend.customer.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    /**
     * Find all categories sorted by display order for admin management
     */
    List<Category> findAllByOrderByDisplayOrderAsc();
    
    /**
     * Find only active categories sorted by display order for customer display
     */
    List<Category> findByActiveTrueOrderByDisplayOrderAsc();
    
    /**
     * Check if a category with the given name already exists
     */
    boolean existsByName(String name);
    
    /**
     * Check if a category with the given name exists, excluding a specific ID (for updates)
     */
    boolean existsByNameAndIdNot(String name, Long id);
    
    /**
     * Find category by name
     */
    Optional<Category> findByName(String name);
}
