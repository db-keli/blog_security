package org.example.blog_spring.repository;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.example.blog_spring.domain.Comment;
import org.example.blog_spring.domain.Post;
import org.example.blog_spring.domain.PostStatus;
import org.example.blog_spring.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

class CommentRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

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

    private Comment buildComment(Long postId, Long userId, String content) {
        var now = Instant.now();
        return Comment.builder().postId(postId).userId(userId).content(content).createdAt(now)
                .updatedAt(now).build();
    }

    @Test
    void findByPostId_returnsCommentsForPost() {
        var userId = ensureUser("u1", "u1@example.com");
        var postId = ensurePost(userId);

        commentRepository.save(buildComment(postId, userId, "c1"));
        commentRepository.save(buildComment(postId, userId, "c2"));

        var page = commentRepository.findByPostId(postId, PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(2);
    }

    @Test
    void findByUserId_returnsCommentsForUser() {
        var userId1 = ensureUser("u1", "u1@example.com");
        var userId2 = ensureUser("u2", "u2@example.com");
        var postId = ensurePost(userId1);

        commentRepository.save(buildComment(postId, userId1, "by u1"));
        commentRepository.save(buildComment(postId, userId2, "by u2"));

        var page = commentRepository.findByUserId(userId1, PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().getUserId()).isEqualTo(userId1);
    }

    @Test
    void countByPostId_countsCommentsCorrectly() {
        var userId = ensureUser("u1", "u1@example.com");
        var postId = ensurePost(userId);

        commentRepository.save(buildComment(postId, userId, "c1"));
        commentRepository.save(buildComment(postId, userId, "c2"));

        assertThat(commentRepository.countByPostId(postId)).isEqualTo(2L);
    }
}

