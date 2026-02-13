package org.example.blog_spring.dto;

import java.util.Set;

import org.example.blog_spring.domain.PostStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePostRequest(@NotBlank @Size(max = 255) String title,

                @NotBlank String content,

                @NotBlank @Size(max = 255) String slug,

                PostStatus status,

                Set<Long> tagIds) {
}

