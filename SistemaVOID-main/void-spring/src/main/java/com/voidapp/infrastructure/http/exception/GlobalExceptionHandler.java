package com.voidapp.infrastructure.http.exception;

import com.voidapp.core.domain.exception.DomainException;
import com.voidapp.core.domain.exception.InvalidCredentialsError;
import com.voidapp.core.domain.exception.UserNotFoundError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsError.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentials(InvalidCredentialsError ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundError.class)
    public ResponseEntity<Map<String, String>> handleUserNotFound(UserNotFoundError ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage()); // Returns 401 for security reasons like original JS
    }

    @ExceptionHandler(com.voidapp.core.domain.exception.DuplicateEmailError.class)
    public ResponseEntity<Map<String, String>> handleDuplicateEmail(com.voidapp.core.domain.exception.DuplicateEmailError ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(com.voidapp.core.domain.exception.ConcurrentStreamLimitExceededError.class)
    public ResponseEntity<Map<String, String>> handleConcurrentStream(com.voidapp.core.domain.exception.ConcurrentStreamLimitExceededError ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage()); // Returns 403
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<Map<String, String>> handleDomainException(DomainException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }

    private ResponseEntity<Map<String, String>> buildResponse(@NonNull HttpStatus status, String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return ResponseEntity.status(status).body(response);
    }
}
