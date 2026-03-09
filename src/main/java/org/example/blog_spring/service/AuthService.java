package org.example.blog_spring.service;

import org.example.blog_spring.dto.LoginRequest;
import org.example.blog_spring.dto.LoginResponse;
import org.example.blog_spring.dto.RegisterRequest;
import org.example.blog_spring.dto.UserDto;

public interface AuthService {

    UserDto register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    void logout(String token);
}

