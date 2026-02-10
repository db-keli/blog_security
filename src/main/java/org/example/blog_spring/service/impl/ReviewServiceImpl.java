package org.example.blog_spring.service.impl;

import org.example.blog_spring.dao.PostDao;
import org.example.blog_spring.dao.ReviewDao;
import org.example.blog_spring.dao.UserDao;
import org.example.blog_spring.dto.CreateReviewRequest;
import org.example.blog_spring.dto.ReviewDto;
import org.example.blog_spring.dto.UpdateReviewRequest;
import org.example.blog_spring.exception.PostNotFoundException;
import org.example.blog_spring.exception.ReviewNotFoundException;
import org.example.blog_spring.exception.UserNotFoundException;
import org.example.blog_spring.mapper.ReviewMapper;
import org.example.blog_spring.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewDao reviewDao;
    private final PostDao postDao;
    private final UserDao userDao;

    public ReviewServiceImpl(ReviewDao reviewDao, PostDao postDao, UserDao userDao) {
        this.reviewDao = reviewDao;
        this.postDao = postDao;
        this.userDao = userDao;
    }

    @Override
    public ReviewDto createReview(CreateReviewRequest request) {
        if (!postDao.existsById(request.postId())) {
            throw new PostNotFoundException(request.postId());
        }
        if (!userDao.existsById(request.userId())) {
            throw new UserNotFoundException(request.userId());
        }

        reviewDao.findByPostIdAndUserId(request.postId(), request.userId())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("User %d has already reviewed post %d"
                            .formatted(request.userId(), request.postId()));
                });

        var review = ReviewMapper.toEntity(request);
        var saved = reviewDao.insert(review);
        return ReviewMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewDto getReview(Long id) {
        var review = reviewDao.findById(id).orElseThrow(() -> new ReviewNotFoundException(id));
        return ReviewMapper.toDto(review);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewDto getReviewForUserAndPost(Long userId, Long postId) {
        var review = reviewDao.findByPostIdAndUserId(postId, userId)
                .orElseThrow(() -> new ReviewNotFoundException(userId, postId));
        return ReviewMapper.toDto(review);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewDto> getReviewsForPost(Long postId, Pageable pageable) {
        return reviewDao.findByPostId(postId, pageable).map(ReviewMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewDto> getReviewsForUser(Long userId, Pageable pageable) {
        return reviewDao.findByUserId(userId, pageable).map(ReviewMapper::toDto);
    }

    @Override
    public ReviewDto updateReview(Long id, UpdateReviewRequest request) {
        var review = reviewDao.findById(id).orElseThrow(() -> new ReviewNotFoundException(id));

        ReviewMapper.updateEntity(review, request);
        reviewDao.update(review);
        return ReviewMapper.toDto(review);
    }

    @Override
    public void deleteReview(Long id) {
        if (!reviewDao.existsById(id)) {
            throw new ReviewNotFoundException(id);
        }
        reviewDao.deleteById(id);
    }
}
