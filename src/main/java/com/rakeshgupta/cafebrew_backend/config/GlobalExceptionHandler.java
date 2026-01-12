package com.rakeshgupta.cafebrew_backend.config;

import com.rakeshgupta.cafebrew_backend.common.exception.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle MaxUploadSizeExceededException - thrown when file upload exceeds max size.
     * Returns 413 Payload Too Large.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        log.warn("File upload size exceeded: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.PAYLOAD_TOO_LARGE, "File size exceeds maximum limit of 10MB");
    }

    /**
     * Handle CategoryHasItemsException - thrown when trying to delete a category with menu items.
     * Returns 400 Bad Request.
     */
    @ExceptionHandler(CategoryHasItemsException.class)
    public ResponseEntity<Map<String, Object>> handleCategoryHasItems(CategoryHasItemsException ex) {
        log.warn("Category has items: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Handle DuplicateCategoryNameException - thrown when creating/updating a category with duplicate name.
     * Returns 400 Bad Request.
     */
    @ExceptionHandler(DuplicateCategoryNameException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateCategoryName(DuplicateCategoryNameException ex) {
        log.warn("Duplicate category name: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Handle CategoryNotFoundException - thrown when a category is not found.
     * Returns 404 Not Found.
     */
    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCategoryNotFound(CategoryNotFoundException ex) {
        log.warn("Category not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Handle MenuItemNotFoundException - thrown when a menu item is not found.
     * Returns 404 Not Found.
     */
    @ExceptionHandler(MenuItemNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleMenuItemNotFound(MenuItemNotFoundException ex) {
        log.warn("Menu item not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Handle ExtraIngredientNotFoundException - thrown when an extra ingredient is not found.
     * Returns 404 Not Found.
     */
    @ExceptionHandler(ExtraIngredientNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleExtraIngredientNotFound(ExtraIngredientNotFoundException ex) {
        log.warn("Extra ingredient not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Handle DuplicateExtraIngredientNameException - thrown when creating/updating an extra ingredient with duplicate name.
     * Returns 409 Conflict.
     */
    @ExceptionHandler(DuplicateExtraIngredientNameException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateExtraIngredientName(DuplicateExtraIngredientNameException ex) {
        log.warn("Duplicate extra ingredient name: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Handle ExtraIngredientHasOrdersException - thrown when trying to delete an extra ingredient with order history.
     * Returns 409 Conflict.
     */
    @ExceptionHandler(ExtraIngredientHasOrdersException.class)
    public ResponseEntity<Map<String, Object>> handleExtraIngredientHasOrders(ExtraIngredientHasOrdersException ex) {
        log.warn("Extra ingredient has orders: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Handle InvalidCategoryException - thrown when one or more category IDs are invalid.
     * Returns 400 Bad Request.
     */
    @ExceptionHandler(InvalidCategoryException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCategory(InvalidCategoryException ex) {
        log.warn("Invalid category: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Handle InactiveExtraIngredientException - thrown when trying to use an inactive extra ingredient.
     * Returns 400 Bad Request.
     */
    @ExceptionHandler(InactiveExtraIngredientException.class)
    public ResponseEntity<Map<String, Object>> handleInactiveExtraIngredient(InactiveExtraIngredientException ex) {
        log.warn("Inactive extra ingredient: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Handle generic EntityNotFoundException for any other entity not found cases.
     * Returns 404 Not Found.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFound(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + ", " + b)
                .orElse("Validation failed");
        log.warn("Validation error: {}", message);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        log.error("Internal error: {}", ex.getMessage(), ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    /**
     * Build a consistent error response with timestamp, status, error, and message fields.
     * This ensures all error responses follow the same format as specified in Requirements 9.1, 9.2, 9.3.
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }
}
