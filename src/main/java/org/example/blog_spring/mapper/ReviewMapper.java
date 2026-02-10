package org.example.blog_spring.mapper;

import org.example.blog_spring.domain.Review;
import org.example.blog_spring.dto.CreateReviewRequest;
import org.example.blog_spring.dto.ReviewDto;
import org.example.blog_spring.dto.UpdateReviewRequest;

public final class ReviewMapper {

    private ReviewMapper() {}

    public static Review toEntity(CreateReviewRequest request) {
        var review = new Review(request.postId(), request.userId(), request.rating());
        review.setTitle(request.title());
        review.setContent(request.content());
        if (request.verified() != null) {
            review.setVerified(request.verified());
        }
        return review;
    }

    public static void updateEntity(Review review, UpdateReviewRequest request) {
        review.setRating(request.rating());
        review.setTitle(request.title());
        review.setContent(request.content());
        if (request.verified() != null) {
            review.setVerified(request.verified());
        }
        review.setUpdatedAt(java.time.Instant.now());
    }

    public static ReviewDto toDto(Review review) {
        return new ReviewDto(review.getId(), review.getPostId(), review.getUserId(),
                review.getRating(), review.getTitle(), review.getContent(), review.isVerified(),
                review.getCreatedAt(), review.getUpdatedAt());
    }
}
