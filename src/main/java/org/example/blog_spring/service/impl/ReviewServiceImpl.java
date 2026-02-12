package org.example.blog_spring.service.impl;

import org.example.blog_spring.dto.CreateReviewRequest;
import org.example.blog_spring.dto.ReviewDto;
import org.example.blog_spring.dto.UpdateReviewRequest;
import org.example.blog_spring.exception.PostNotFoundException;
import org.example.blog_spring.exception.ReviewNotFoundException;
import org.example.blog_spring.exception.UserNotFoundException;
import org.example.blog_spring.mapper.ReviewMapper;
import org.example.blog_spring.repository.PostRepository;
import org.example.blog_spring.repository.ReviewRepository;
import org.example.blog_spring.repository.UserRepository;
import org.example.blog_spring.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository, PostRepository postRepository,
            UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ReviewDto createReview(CreateReviewRequest request) {
        if (!postRepository.existsById(request.postId())) {
            throw new PostNotFoundException(request.postId());
        }
        if (!userRepository.existsById(request.userId())) {
            throw new UserNotFoundException(request.userId());
        }

        reviewRepository.findByPostIdAndUserId(request.postId(), request.userId())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("User %d has already reviewed post %d"
                            .formatted(request.userId(), request.postId()));
                });

        var review = ReviewMapper.toEntity(request);
        var saved = reviewRepository.save(review);
        return ReviewMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewDto getReview(Long id) {
        var review = reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException(id));
        return ReviewMapper.toDto(review);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewDto getReviewForUserAndPost(Long userId, Long postId) {
        var review = reviewRepository.findByPostIdAndUserId(postId, userId)
                .orElseThrow(() -> new ReviewNotFoundException(userId, postId));
        return ReviewMapper.toDto(review);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewDto> getReviewsForPost(Long postId, Pageable pageable) {
        return reviewRepository.findByPostId(postId, pageable).map(ReviewMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewDto> getReviewsForUser(Long userId, Pageable pageable) {
        return reviewRepository.findByUserId(userId, pageable).map(ReviewMapper::toDto);
    }

    @Override
    public ReviewDto updateReview(Long id, UpdateReviewRequest request) {
        var review = reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException(id));

        ReviewMapper.updateEntity(review, request);
        var saved = reviewRepository.save(review);
        return ReviewMapper.toDto(saved);
    }

    @Override
    public void deleteReview(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new ReviewNotFoundException(id);
        }
        reviewRepository.deleteById(id);
    }
}
