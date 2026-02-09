package org.example.blog_spring.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateCommentRequest(
        @NotBlank
        String content
) {
}

