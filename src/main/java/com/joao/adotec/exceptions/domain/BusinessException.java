package com.joao.adotec.exceptions.domain;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an operation violates a business rule.
 * Maps to HTTP 409 Conflict or HTTP 422 Unprocessable Entity.
 */
public class BusinessException extends DomainException {

    public BusinessException(String message) {
        super(HttpStatus.CONFLICT, message);
    }

    public BusinessException(HttpStatus status, String message) {
        super(status, message);
    }
}
