package org.example.blog_spring.exception;

public class EmailAlreadyUsedException extends RuntimeException {

    public EmailAlreadyUsedException(String email) {
        super("Email '%s' is already in use".formatted(email));
    }
}

