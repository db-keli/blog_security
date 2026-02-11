package org.example.blog_spring.exception;

public class CommentNotFoundException extends RuntimeException {

    public CommentNotFoundException(Long id) {
        super("Comment with id %d not found".formatted(id));
    }
}

