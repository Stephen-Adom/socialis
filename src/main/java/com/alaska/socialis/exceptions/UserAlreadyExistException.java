package com.alaska.socialis.exceptions;

import org.springframework.http.HttpStatus;

public class UserAlreadyExistException extends Exception {

    private HttpStatus status;

    public UserAlreadyExistException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return this.status;
    }
}
