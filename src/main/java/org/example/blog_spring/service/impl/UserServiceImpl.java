package org.example.blog_spring.service.impl;

import org.example.blog_spring.dto.CreateUserRequest;
import org.example.blog_spring.dto.UpdateUserRequest;
import org.example.blog_spring.dto.UserDto;
import org.example.blog_spring.exception.EmailAlreadyUsedException;
import org.example.blog_spring.exception.UserNotFoundException;
import org.example.blog_spring.mapper.UserMapper;
import org.example.blog_spring.repository.UserRepository;
import org.example.blog_spring.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDto createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException(
                    "Username '%s' is already in use".formatted(request.username()));
        }
        var user = UserMapper.toEntity(request);
        var saved = userRepository.save(user);
        return UserMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUser(Long id) {
        var user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        return UserMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserMapper::toDto);
    }

    @Override
    public UserDto updateUser(Long id, UpdateUserRequest request) {
        var user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        if (!user.getEmail().equals(request.email())
                && userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyUsedException(request.email());
        }

        UserMapper.updateEntity(user, request);
        var saved = userRepository.save(user);
        return UserMapper.toDto(saved);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }
}
