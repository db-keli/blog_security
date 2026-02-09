package org.example.blog_spring.dto;

import java.time.Instant;

public record TagDto(
        Long id,
        String name,
        String slug,
        String description,
        Instant createdAt
) {
}

