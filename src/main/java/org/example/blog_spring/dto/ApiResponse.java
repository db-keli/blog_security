package org.example.blog_spring.dto;

import org.springframework.http.HttpStatus;

public record ApiResponse<T>(int status, String message, T data) {

    public static <T> ApiResponse<T> success(HttpStatus status, String message, T data) {
        return new ApiResponse<>(status.value(), message, data);
    }

    public static <T> ApiResponse<T> error(HttpStatus status, String message, T data) {
        return new ApiResponse<>(status.value(), message, data);
    }
}

