package com.rakeshgupta.cafebrew_backend.common.exception;

/**
 * Exception thrown when attempting to use an inactive extra ingredient in an order.
 */
public class InactiveExtraIngredientException extends RuntimeException {
    
    public InactiveExtraIngredientException(String message) {
        super(message);
    }
    
    public InactiveExtraIngredientException(Long extraIngredientId) {
        super("Extra ingredient with id " + extraIngredientId + " is no longer available");
    }
}
