package com.rakeshgupta.cafebrew_backend.common.exception;

/**
 * Exception thrown when attempting to create or update an extra ingredient with a name that already exists.
 */
public class DuplicateExtraIngredientNameException extends RuntimeException {
    
    public DuplicateExtraIngredientNameException(String name) {
        super("Extra ingredient with name '" + name + "' already exists");
    }
}
