package org.example.blog_spring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record CreatePostRequest(
        @NotNull
        Long authorId,

        @NotBlank
        @Size(max = 255)
        String title,

        @NotBlank
        String content,

        @NotBlank
        @Size(max = 255)
        String slug,

        Set<Long> tagIds
) {
}

