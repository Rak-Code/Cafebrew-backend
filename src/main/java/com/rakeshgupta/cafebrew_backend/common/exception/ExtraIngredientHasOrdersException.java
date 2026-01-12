package com.rakeshgupta.cafebrew_backend.common.exception;

/**
 * Exception thrown when attempting to delete an extra ingredient that has associated order history.
 */
public class ExtraIngredientHasOrdersException extends RuntimeException {
    
    public ExtraIngredientHasOrdersException(String ingredientName, long orderCount) {
        super("Cannot delete extra ingredient '" + ingredientName + "' because it has " + orderCount + " order records");
    }
}
