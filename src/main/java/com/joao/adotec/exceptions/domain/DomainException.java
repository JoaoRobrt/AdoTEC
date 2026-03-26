package com.joao.adotec.exceptions.domain;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DomainException extends RuntimeException {
    private final HttpStatus status;

    public DomainException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public DomainException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}
