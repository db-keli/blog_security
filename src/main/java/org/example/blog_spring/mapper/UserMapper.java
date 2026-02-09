package org.example.blog_spring.mapper;

import org.example.blog_spring.domain.User;
import org.example.blog_spring.dto.CreateUserRequest;
import org.example.blog_spring.dto.UpdateUserRequest;
import org.example.blog_spring.dto.UserDto;

public final class UserMapper {

    private UserMapper() {}

    public static User toEntity(CreateUserRequest request) {
        return new User(request.username(), request.email(), request.fullName());
    }

    public static void updateEntity(User user, UpdateUserRequest request) {
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFullName(request.fullName());
    }

    public static UserDto toDto(User user) {
        return new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getFullName(),
                user.getCreatedAt(), user.getUpdatedAt());
    }
}

