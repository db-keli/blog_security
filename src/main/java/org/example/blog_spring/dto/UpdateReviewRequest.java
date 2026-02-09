package org.example.blog_spring.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpdateReviewRequest(
        @Min(1)
        @Max(5)
        short rating,

        String title,

        String content,

        Boolean verified
) {
}

