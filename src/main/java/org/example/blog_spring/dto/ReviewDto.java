package org.example.blog_spring.dto;

import java.time.Instant;

public record ReviewDto(
        Long id,
        Long postId,
        Long userId,
        short rating,
        String title,
        String content,
        boolean verified,
        Instant createdAt,
        Instant updatedAt
) {
}

