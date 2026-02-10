package org.example.blog_spring.exception;

public class PostNotFoundException extends RuntimeException {

    public PostNotFoundException(Long id) {
        super("Post with id %d not found".formatted(id));
    }

    public PostNotFoundException(String slug) {
        super("Post with slug '%s' not found".formatted(slug));
    }
}

