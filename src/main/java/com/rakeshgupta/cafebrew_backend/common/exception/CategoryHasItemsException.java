package com.rakeshgupta.cafebrew_backend.common.exception;

/**
 * Exception thrown when attempting to delete a category that has associated menu items.
 */
public class CategoryHasItemsException extends RuntimeException {
    
    public CategoryHasItemsException(String categoryName, int itemCount) {
        super("Cannot delete category '" + categoryName + "' because it has " + itemCount + " menu items");
    }
}
