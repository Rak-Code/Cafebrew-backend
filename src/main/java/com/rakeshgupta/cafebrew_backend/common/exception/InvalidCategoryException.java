package com.rakeshgupta.cafebrew_backend.common.exception;

/**
 * Exception thrown when one or more category IDs are invalid.
 */
public class InvalidCategoryException extends RuntimeException {
    
    public InvalidCategoryException(String message) {
        super(message);
    }
    
    public InvalidCategoryException(Long categoryId) {
        super("Invalid category ID: " + categoryId);
    }
}
