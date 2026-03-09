package org.example.blog_spring.service.impl;

import java.time.Instant;
import org.example.blog_spring.domain.User;
import org.example.blog_spring.dto.LoginRequest;
import org.example.blog_spring.dto.LoginResponse;
import org.example.blog_spring.dto.RegisterRequest;
import org.example.blog_spring.dto.UserDto;
import org.example.blog_spring.exception.EmailAlreadyUsedException;
import org.example.blog_spring.exception.InvalidCredentialsException;
import org.example.blog_spring.mapper.UserMapper;
import org.example.blog_spring.repository.UserRepository;
import org.example.blog_spring.security.BlogUserDetails;
import org.example.blog_spring.security.JwtTokenProvider;
import org.example.blog_spring.security.SecurityMetrics;
import org.example.blog_spring.security.TokenBlacklist;
import org.example.blog_spring.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklist tokenBlacklist;
    private final SecurityMetrics securityMetrics;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider, TokenBlacklist tokenBlacklist,
            SecurityMetrics securityMetrics) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenBlacklist = tokenBlacklist;
        this.securityMetrics = securityMetrics;
    }

    @Override
    public UserDto register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException(
                    "Username '%s' is already in use".formatted(request.username()));
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyUsedException(request.email());
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setDisplayName(request.fullName());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        Instant now = Instant.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        User saved = userRepository.save(user);
        return UserMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        var user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            securityMetrics.incrementLoginFailure();
            throw new InvalidCredentialsException();
        }

        String token = jwtTokenProvider.createToken(new BlogUserDetails(user));
        securityMetrics.incrementLoginSuccess();
        return new LoginResponse(token, UserMapper.toDto(user));
    }

    @Override
    public void logout(String token) {
        tokenBlacklist.revoke(token);
    }
}

