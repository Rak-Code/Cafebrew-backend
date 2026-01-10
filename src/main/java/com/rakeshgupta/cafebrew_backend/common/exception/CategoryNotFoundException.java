package com.rakeshgupta.cafebrew_backend.common.exception;

import jakarta.persistence.EntityNotFoundException;

/**
 * Exception thrown when a category is not found by its ID.
 */
public class CategoryNotFoundException extends EntityNotFoundException {
    
    public CategoryNotFoundException(Long id) {
        super("Category not found with id: " + id);
    }
}
