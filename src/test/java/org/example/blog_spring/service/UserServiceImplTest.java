package org.example.blog_spring.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;

import org.example.blog_spring.dao.UserDao;
import org.example.blog_spring.domain.User;
import org.example.blog_spring.dto.CreateUserRequest;
import org.example.blog_spring.dto.UpdateUserRequest;
import org.example.blog_spring.exception.EmailAlreadyUsedException;
import org.example.blog_spring.exception.UserNotFoundException;
import org.example.blog_spring.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserDao userDao;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userDao);
    }

    @Test
    void getUser_returnsUser() {
        var user =
                User.builder().id(1L).username("jdoe").email("j@e.com").displayName("John").build();
        given(userDao.findById(1L)).willReturn(Optional.of(user));

        var result = userService.getUser(1L);

        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo("jdoe");
    }

    @Test
    void getUser_throws_whenNotFound() {
        given(userDao.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(999L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void createUser_throws_whenUsernameExists() {
        given(userDao.existsByUsername("jdoe")).willReturn(true);
        var request = new CreateUserRequest("jdoe", "j@e.com", "John");

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("jdoe");
    }

    @Test
    void createUser_savesUser() {
        given(userDao.existsByUsername("newuser")).willReturn(false);
        given(userDao.insert(any(User.class))).willAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        var request = new CreateUserRequest("newuser", "new@e.com", "New User");
        var result = userService.createUser(request);

        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo("newuser");
    }

    @Test
    void updateUser_throws_whenEmailAlreadyUsed() {
        var user = User.builder().id(1L).username("jdoe").email("old@e.com").displayName("John")
                .build();
        given(userDao.findById(1L)).willReturn(Optional.of(user));
        given(userDao.existsByEmail("other@e.com")).willReturn(true);
        var request = new UpdateUserRequest("jdoe", "other@e.com", "John");

        assertThatThrownBy(() -> userService.updateUser(1L, request))
                .isInstanceOf(EmailAlreadyUsedException.class);
    }

    @Test
    void deleteUser_throws_whenNotFound() {
        given(userDao.existsById(999L)).willReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(UserNotFoundException.class);
    }
}
