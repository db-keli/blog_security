package org.example.blog_spring.dto;

import java.time.Instant;

public record UserDto(Long id, String username, String email, String fullName, Instant createdAt,
                Instant updatedAt) {
}

