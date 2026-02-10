package org.example.blog_spring.web.graphql;

import static org.mockito.BDDMockito.given;

import java.time.Instant;
import java.util.List;
import org.example.blog_spring.dto.PostDto;
import org.example.blog_spring.dto.TagSummaryDto;
import org.example.blog_spring.service.CommentService;
import org.example.blog_spring.service.PostService;
import org.example.blog_spring.service.ReviewService;
import org.example.blog_spring.service.TagService;
import org.example.blog_spring.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.test.tester.GraphQlTester;

@GraphQlTest(BlogGraphQlController.class)
class BlogGraphQlControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockBean
    private UserService userService;

    @MockBean
    private PostService postService;

    @MockBean
    private TagService tagService;

    @MockBean
    private CommentService commentService;

    @MockBean
    private ReviewService reviewService;

    @Test
    void postsQuery_returnsPosts() {
        var post = new PostDto(
                1L,
                1L,
                "Title",
                "Content",
                "slug",
                org.example.blog_spring.domain.PostStatus.PUBLISHED,
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
                Mockito.any()
        )).willReturn(page);

        graphQlTester.document("""
                        query {
                          posts(page: 0, size: 20) {
                            id
                            title
                            slug
                          }
                        }
                        """)
                .execute()
                .path("posts[0].title")
                .entity(String.class)
                .isEqualTo("Title");
    }
}

