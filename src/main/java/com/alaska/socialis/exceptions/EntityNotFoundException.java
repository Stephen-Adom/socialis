package com.alaska.socialis.exceptions;

import org.springframework.http.HttpStatus;

public class EntityNotFoundException extends Exception {

    private HttpStatus status;

    public EntityNotFoundException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return this.status;
    }
}