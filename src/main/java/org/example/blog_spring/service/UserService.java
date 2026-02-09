package org.example.blog_spring.service;

import org.example.blog_spring.dto.CreateUserRequest;
import org.example.blog_spring.dto.UpdateUserRequest;
import org.example.blog_spring.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserDto createUser(CreateUserRequest request);

    UserDto getUser(Long id);

    Page<UserDto> getUsers(Pageable pageable);

    UserDto updateUser(Long id, UpdateUserRequest request);

    void deleteUser(Long id);
}

