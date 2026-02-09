package org.example.blog_spring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCommentRequest(
        @NotNull
        Long postId,

        @NotNull
        Long userId,

        Long parentId,

        @NotBlank
        String content
) {
}

