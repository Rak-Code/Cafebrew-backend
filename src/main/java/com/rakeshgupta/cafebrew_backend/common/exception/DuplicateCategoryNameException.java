package com.rakeshgupta.cafebrew_backend.common.exception;

/**
 * Exception thrown when attempting to create or update a category with a name that already exists.
 */
public class DuplicateCategoryNameException extends RuntimeException {
    
    public DuplicateCategoryNameException(String name) {
        super("Category with name '" + name + "' already exists");
    }
}
