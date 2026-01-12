package com.rakeshgupta.cafebrew_backend.common.exception;

import jakarta.persistence.EntityNotFoundException;

/**
 * Exception thrown when an extra ingredient is not found by its ID.
 */
public class ExtraIngredientNotFoundException extends EntityNotFoundException {
    
    public ExtraIngredientNotFoundException(Long id) {
        super("Extra ingredient not found with id: " + id);
    }
}
