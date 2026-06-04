package com.finance.tracker.exception;

/**
 * ResourceNotFoundException
 *
 * Thrown when a requested resource (e.g., a transaction by ID) does not exist in MongoDB.
 * Triggers a 404 Not Found HTTP response via GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, String fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
