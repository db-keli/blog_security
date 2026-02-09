package org.example.blog_spring.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateReviewRequest(
        @NotNull
        Long postId,

        @NotNull
        Long userId,

        @Min(1)
        @Max(5)
        short rating,

        String title,

        String content,

        Boolean verified
) {
}

