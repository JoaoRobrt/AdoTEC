package com.joao.adotec.exceptions.domain;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a requested resource is not found in the system.
 * Maps to HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends DomainException {

    public ResourceNotFoundException(String resourceName, Long id) {
        super(HttpStatus.NOT_FOUND, resourceName + " not found with id: " + id);
    }

    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
