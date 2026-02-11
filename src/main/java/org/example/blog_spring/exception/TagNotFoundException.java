package org.example.blog_spring.exception;

public class TagNotFoundException extends RuntimeException {

    public TagNotFoundException(Long id) {
        super("Tag with id %d not found".formatted(id));
    }

    public TagNotFoundException(String slug) {
        super("Tag with slug '%s' not found".formatted(slug));
    }
}

