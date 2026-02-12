package org.example.blog_spring.dto;

import java.time.Instant;
import java.util.Set;
import org.example.blog_spring.domain.PostStatus;

public record PostDto(
        Long id,
        Long authorId,
        String title,
        String content,
        String slug,
        PostStatus status,
        Instant createdAt,
        Instant updatedAt,
        Instant publishedAt,
        long commentCount,
        Set<TagSummaryDto> tags
) {
}

