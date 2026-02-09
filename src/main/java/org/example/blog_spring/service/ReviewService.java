package org.example.blog_spring.service;

import org.example.blog_spring.dto.CreateReviewRequest;
import org.example.blog_spring.dto.ReviewDto;
import org.example.blog_spring.dto.UpdateReviewRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    ReviewDto createReview(CreateReviewRequest request);

    ReviewDto getReview(Long id);

    ReviewDto getReviewForUserAndPost(Long userId, Long postId);

    Page<ReviewDto> getReviewsForPost(Long postId, Pageable pageable);

    Page<ReviewDto> getReviewsForUser(Long userId, Pageable pageable);

    ReviewDto updateReview(Long id, UpdateReviewRequest request);

    void deleteReview(Long id);
}

