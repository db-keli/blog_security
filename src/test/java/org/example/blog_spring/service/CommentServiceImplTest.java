package org.example.blog_spring.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.example.blog_spring.domain.Comment;
import org.example.blog_spring.dto.CreateCommentRequest;
import org.example.blog_spring.exception.CommentNotFoundException;
import org.example.blog_spring.exception.PostNotFoundException;
import org.example.blog_spring.exception.UserNotFoundException;
import org.example.blog_spring.service.impl.CommentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private org.example.blog_spring.repository.CommentRepository commentRepository;
    @Mock
    private org.example.blog_spring.repository.PostRepository postRepository;
    @Mock
    private org.example.blog_spring.repository.UserRepository userRepository;

    private CommentServiceImpl commentService;

    @BeforeEach
    void setUp() {
        commentService = new CommentServiceImpl(commentRepository, postRepository, userRepository);
    }

    @Test
    void createComment_throws_whenPostNotFound() {
        given(postRepository.existsById(999L)).willReturn(false);
        var request = new CreateCommentRequest(999L, 1L, null, "Content");

        assertThatThrownBy(() -> commentService.createComment(request))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    void createComment_throws_whenUserNotFound() {
        given(postRepository.existsById(1L)).willReturn(true);
        given(userRepository.existsById(999L)).willReturn(false);
        var request = new CreateCommentRequest(1L, 999L, null, "Content");

        assertThatThrownBy(() -> commentService.createComment(request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getComment_throws_whenNotFound() {
        given(commentRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.getComment(999L))
                .isInstanceOf(CommentNotFoundException.class);
    }
}
