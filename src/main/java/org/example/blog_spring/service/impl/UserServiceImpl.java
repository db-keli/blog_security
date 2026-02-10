package org.example.blog_spring.service.impl;

import org.example.blog_spring.dao.UserDao;
import org.example.blog_spring.dto.CreateUserRequest;
import org.example.blog_spring.dto.UpdateUserRequest;
import org.example.blog_spring.dto.UserDto;
import org.example.blog_spring.exception.EmailAlreadyUsedException;
import org.example.blog_spring.exception.UserNotFoundException;
import org.example.blog_spring.mapper.UserMapper;
import org.example.blog_spring.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public UserDto createUser(CreateUserRequest request) {
        if (userDao.existsByUsername(request.username())) {
            throw new IllegalArgumentException(
                    "Username '%s' is already in use".formatted(request.username()));
        }
        var user = UserMapper.toEntity(request);
        var saved = userDao.insert(user);
        return UserMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUser(Long id) {
        var user = userDao.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        return UserMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getUsers(Pageable pageable) {
        return userDao.findAll(pageable).map(UserMapper::toDto);
    }

    @Override
    public UserDto updateUser(Long id, UpdateUserRequest request) {
        var user = userDao.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        if (!user.getEmail().equals(request.email())
                && userDao.existsByEmail(request.email())) {
            throw new EmailAlreadyUsedException(request.email());
        }

        UserMapper.updateEntity(user, request);
        userDao.update(user);
        return UserMapper.toDto(user);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userDao.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userDao.deleteById(id);
    }
}
