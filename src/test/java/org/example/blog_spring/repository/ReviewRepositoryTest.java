package org.example.blog_spring.repository;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.example.blog_spring.domain.Post;
import org.example.blog_spring.domain.PostStatus;
import org.example.blog_spring.domain.Review;
import org.example.blog_spring.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

class ReviewRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    private String unique() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private Long ensureUser(String username, String email) {
        var id = unique();
        var localPart = email.substring(0, email.indexOf('@'));
        return userRepository.save(User.builder().username(username + "-" + id)
                .email(localPart + "-" + id + "@example.com")
                .displayName(username).passwordHash("").createdAt(Instant.now()).updatedAt(Instant.now()).build())
                .getId();
    }

    private Long ensurePost(Long authorId) {
        var now = Instant.now();
        return postRepository
                .save(Post.builder().authorId(authorId).title("Title").content("Content")
                        .slug("slug-" + authorId + "-" + now.toEpochMilli() + "-" + unique())
                        .status(PostStatus.PUBLISHED).createdAt(now).updatedAt(now).build())
                .getId();
    }

    private Review buildReview(Long postId, Long userId, short rating) {
        var now = Instant.now();
        return Review.builder().postId(postId).userId(userId).rating(rating).createdAt(now)
                .updatedAt(now).verified(false).build();
    }

    @Test
    void findByPostIdAndUserId_returnsReview() {
        var userId = ensureUser("u1", "u1@example.com");
        var postId = ensurePost(userId);

        var saved = reviewRepository.save(buildReview(postId, userId, (short) 5));

        var found = reviewRepository.findByPostIdAndUserId(postId, userId);
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    void findByPostId_returnsReviewsForPost() {
        var userId1 = ensureUser("u1", "u1@example.com");
        var userId2 = ensureUser("u2", "u2@example.com");
        var postId = ensurePost(userId1);

        reviewRepository.save(buildReview(postId, userId1, (short) 4));
        reviewRepository.save(buildReview(postId, userId2, (short) 5));

        var page = reviewRepository.findByPostId(postId, PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(2);
    }

    @Test
    void findByUserId_returnsReviewsForUser() {
        var userId1 = ensureUser("u1", "u1@example.com");
        var userId2 = ensureUser("u2", "u2@example.com");
        var postId = ensurePost(userId1);

        reviewRepository.save(buildReview(postId, userId1, (short) 4));
        reviewRepository.save(buildReview(postId, userId2, (short) 5));

        var page = reviewRepository.findByUserId(userId1, PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().getUserId()).isEqualTo(userId1);
    }
}

