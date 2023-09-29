package com.alaska.socialis.exceptions;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;

public class ValidationErrorsException extends Exception {

    private HttpStatus status;

    private List<FieldError> errors = new ArrayList<>();

    public ValidationErrorsException(List<FieldError> fieldErrors, HttpStatus status) {
        this.errors = fieldErrors;
        this.status = status;
    }

    public ValidationErrorsException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public List<FieldError> getFieldErrors() {
        return this.errors;
    }

    public HttpStatus getStatus() {
        return this.status;
    }

}
