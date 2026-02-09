package org.example.blog_spring.dto;

import java.time.Instant;

public record CommentDto(
        Long id,
        Long postId,
        Long userId,
        Long parentId,
        String content,
        Instant createdAt,
        Instant updatedAt
) {
}

