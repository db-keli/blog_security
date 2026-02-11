package org.example.blog_spring.web.graphql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.Instant;
import java.util.List;

import org.example.blog_spring.domain.PostStatus;
import org.example.blog_spring.dto.PostDto;
import org.example.blog_spring.dto.TagSummaryDto;
import org.example.blog_spring.service.CommentService;
import org.example.blog_spring.service.PostService;
import org.example.blog_spring.service.ReviewService;
import org.example.blog_spring.service.TagService;
import org.example.blog_spring.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class BlogGraphQlControllerTest {

    @Mock
    private UserService userService;
    @Mock
    private PostService postService;
    @Mock
    private TagService tagService;
    @Mock
    private CommentService commentService;
    @Mock
    private ReviewService reviewService;

    private BlogGraphQlController controller;

    @BeforeEach
    void setUp() {
        controller = new BlogGraphQlController(userService, postService, tagService, commentService, reviewService);
    }

    @Test
    void postsQuery_returnsPosts() {
        var post = new PostDto(
                1L,
                1L,
                "Title",
                "Content",
                "slug",
                PostStatus.PUBLISHED,
                Instant.now(),
                Instant.now(),
                Instant.now(),
                java.util.Set.of(new TagSummaryDto(1L, "java", "java"))
        );

        Page<PostDto> page =
                new PageImpl<>(List.of(post), PageRequest.of(0, 20), 1);

        given(postService.getPosts(
                Mockito.isNull(),
                Mockito.isNull(),
                Mockito.isNull(),
                Mockito.isNull(),
                Mockito.any(Pageable.class)
        )).willReturn(page);

        var result = controller.posts(0, 20, null, null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().title()).isEqualTo("Title");
    }

    @Test
    void usersQuery_returnsUsers() {
        var user = new org.example.blog_spring.dto.UserDto(
                1L, "jdoe", "j@e.com", "John", Instant.now(), Instant.now());
        given(userService.getUsers(PageRequest.of(0, 20))).willReturn(new PageImpl<>(List.of(user), PageRequest.of(0, 20), 1));

        var result = controller.users(0, 20);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().username()).isEqualTo("jdoe");
    }
}

