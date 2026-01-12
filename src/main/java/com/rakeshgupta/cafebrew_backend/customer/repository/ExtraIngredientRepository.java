package com.rakeshgupta.cafebrew_backend.customer.repository;

import com.rakeshgupta.cafebrew_backend.customer.entity.ExtraIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExtraIngredientRepository extends JpaRepository<ExtraIngredient, Long> {

    /**
     * Find all extra ingredients sorted by name for admin management.
     */
    List<ExtraIngredient> findAllByOrderByNameAsc();

    /**
     * Find only active extra ingredients sorted by name.
     */
    List<ExtraIngredient> findByActiveTrueOrderByNameAsc();

    /**
     * Check if an extra ingredient with the given name already exists.
     */
    boolean existsByName(String name);

    /**
     * Check if an extra ingredient with the given name exists, excluding a specific ID (for updates).
     */
    boolean existsByNameAndIdNot(String name, Long id);

    /**
     * Find extra ingredient by name.
     */
    Optional<ExtraIngredient> findByName(String name);

    /**
     * Find active extra ingredients by category ID.
     * Only returns ingredients where both the ingredient and category are active.
     */
    @Query("SELECT DISTINCT ei FROM ExtraIngredient ei " +
           "JOIN ei.categories c " +
           "WHERE c.id = :categoryId " +
           "AND ei.active = true " +
           "AND c.active = true " +
           "ORDER BY ei.name ASC")
    List<ExtraIngredient> findActiveByCategory(@Param("categoryId") Long categoryId);

    /**
     * Find active extra ingredients by multiple category IDs.
     * Only returns ingredients where both the ingredient and category are active.
     */
    @Query("SELECT DISTINCT ei FROM ExtraIngredient ei " +
           "JOIN ei.categories c " +
           "WHERE c.id IN :categoryIds " +
           "AND ei.active = true " +
           "AND c.active = true " +
           "ORDER BY ei.name ASC")
    List<ExtraIngredient> findActiveByCategories(@Param("categoryIds") List<Long> categoryIds);

    /**
     * Find all extra ingredients (including inactive) by category ID.
     * Used for admin queries.
     */
    @Query("SELECT DISTINCT ei FROM ExtraIngredient ei " +
           "JOIN ei.categories c " +
           "WHERE c.id = :categoryId " +
           "ORDER BY ei.name ASC")
    List<ExtraIngredient> findByCategory(@Param("categoryId") Long categoryId);

    /**
     * Find extra ingredients with eager loading of categories.
     */
    @Query("SELECT DISTINCT ei FROM ExtraIngredient ei " +
           "LEFT JOIN FETCH ei.categories " +
           "ORDER BY ei.name ASC")
    List<ExtraIngredient> findAllWithCategories();
}
