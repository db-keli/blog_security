package org.example.blog_spring.dto;

public record LoginResponse(String token, UserDto user) {
}

