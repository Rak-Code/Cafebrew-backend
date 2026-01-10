package com.rakeshgupta.cafebrew_backend.common.exception;

import jakarta.persistence.EntityNotFoundException;

/**
 * Exception thrown when a menu item is not found by its ID.
 */
public class MenuItemNotFoundException extends EntityNotFoundException {
    
    public MenuItemNotFoundException(Long id) {
        super("Menu item not found with id: " + id);
    }
}
