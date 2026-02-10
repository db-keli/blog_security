package org.example.blog_spring.exception;

public class ReviewNotFoundException extends RuntimeException {

    public ReviewNotFoundException(Long id) {
        super("Review with id %d not found".formatted(id));
    }

    public ReviewNotFoundException(Long userId, Long postId) {
        super("Review for user %d and post %d not found".formatted(userId, postId));
    }
}

