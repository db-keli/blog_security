package org.example.blog_spring.web.rest;

import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import org.example.blog_spring.dto.ApiResponse;
import org.example.blog_spring.exception.EmailAlreadyUsedException;
import org.example.blog_spring.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFound(UserNotFoundException ex) {
        var response = ApiResponse.<Void>error(HttpStatus.NOT_FOUND, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailAlreadyUsed(EmailAlreadyUsedException ex) {
        var response = ApiResponse.<Void>error(HttpStatus.CONFLICT, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        var response = ApiResponse.error(HttpStatus.BAD_REQUEST, "Validation failed", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolations(
            ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            var field = violation.getPropertyPath().toString();
            errors.put(field, violation.getMessage());
        });

        var response = ApiResponse.error(HttpStatus.BAD_REQUEST, "Validation failed", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        var response = ApiResponse.<Void>error(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        var response =
                ApiResponse.<Void>error(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Unexpected error occurred", null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

