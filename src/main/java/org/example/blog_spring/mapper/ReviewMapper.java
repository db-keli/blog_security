package org.example.blog_spring.mapper;

import org.example.blog_spring.domain.Post;
import org.example.blog_spring.domain.Review;
import org.example.blog_spring.domain.User;
import org.example.blog_spring.dto.CreateReviewRequest;
import org.example.blog_spring.dto.ReviewDto;
import org.example.blog_spring.dto.UpdateReviewRequest;

public final class ReviewMapper {

    private ReviewMapper() {}

    public static Review toEntity(CreateReviewRequest request, Post post, User user) {
        var review = new Review(post, user, request.rating());
        review.setTitle(request.title());
        review.setContent(request.content());
        if (request.verified() != null) {
            review.setVerified(request.verified());
        }
        return review;
    }

    public static void updateEntity(Review review, UpdateReviewRequest request) {
        review.setTitle(request.title());
        review.setContent(request.content());
        if (request.verified() != null) {
            review.setVerified(request.verified());
        }
    }

    public static ReviewDto toDto(Review review) {
        return new ReviewDto(review.getId(), review.getPost().getId(), review.getUser().getId(),
                review.getRating(), review.getTitle(), review.getContent(), review.isVerified(),
                review.getCreatedAt(), review.getUpdatedAt());
    }
}

