package org.example.blog_spring.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.example.blog_spring.dao.PostDao;
import org.example.blog_spring.dao.ReviewDao;
import org.example.blog_spring.dao.UserDao;
import org.example.blog_spring.dto.CreateReviewRequest;
import org.example.blog_spring.exception.PostNotFoundException;
import org.example.blog_spring.exception.ReviewNotFoundException;
import org.example.blog_spring.exception.UserNotFoundException;
import org.example.blog_spring.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewDao reviewDao;
    @Mock
    private PostDao postDao;
    @Mock
    private UserDao userDao;

    private ReviewServiceImpl reviewService;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewServiceImpl(reviewDao, postDao, userDao);
    }

    @Test
    void createReview_throws_whenPostNotFound() {
        given(postDao.existsById(999L)).willReturn(false);
        var request = new CreateReviewRequest(999L, 1L, (short) 5, null, null, null);

        assertThatThrownBy(() -> reviewService.createReview(request))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    void createReview_throws_whenUserNotFound() {
        given(postDao.existsById(1L)).willReturn(true);
        given(userDao.existsById(999L)).willReturn(false);
        var request = new CreateReviewRequest(1L, 999L, (short) 5, null, null, null);

        assertThatThrownBy(() -> reviewService.createReview(request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getReview_throws_whenNotFound() {
        given(reviewDao.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.getReview(999L))
                .isInstanceOf(ReviewNotFoundException.class);
    }
}
