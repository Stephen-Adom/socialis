package com.alaska.socialis.exceptions;

import org.springframework.http.HttpStatus;

public class TokenExpiredException extends Exception {
    private HttpStatus status;

    public TokenExpiredException(String Message, HttpStatus status) {
        super(Message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return this.status;
    }
}
