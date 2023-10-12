package com.alaska.socialis.exceptions;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.alaska.socialis.model.dto.ErrorResponse;
import com.alaska.socialis.model.dto.ValidationErrorResponse;

@ControllerAdvice
@ResponseStatus
public class CustomExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(EntityNotFoundException exception) {
        return new ResponseEntity<ErrorResponse>(new ErrorResponse(exception.getStatus(), exception.getMessage()),
                exception.getStatus());
    }

    @ExceptionHandler(UnauthorizedRequestException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedRequest(UnauthorizedRequestException exception) {
        return new ResponseEntity<ErrorResponse>(new ErrorResponse(exception.getStatus(), exception.getMessage()),
                exception.getStatus());
    }

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExist(UserAlreadyExistException exception) {
        return new ResponseEntity<ErrorResponse>(new ErrorResponse(exception.getStatus(), exception.getMessage()),
                exception.getStatus());
    }

    @ExceptionHandler(ValidationErrorsException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationError(ValidationErrorsException exception) {
        List<String> messages = new ArrayList<String>();

        if (exception.getMessage() != null) {
            messages.add(exception.getMessage());
        } else {
            for (FieldError error : exception.getFieldErrors()) {
                messages.add(error.getDefaultMessage());
            }
        }

        ValidationErrorResponse errormessage = ValidationErrorResponse.builder().status(exception.getStatus())
                .messages(messages).build();

        return new ResponseEntity<ValidationErrorResponse>(errormessage, exception.getStatus());

    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleExpiredToken(TokenExpiredException exception) {
        return new ResponseEntity<ErrorResponse>(new ErrorResponse(exception.getStatus(), exception.getMessage()),
                exception.getStatus());
    }
}
