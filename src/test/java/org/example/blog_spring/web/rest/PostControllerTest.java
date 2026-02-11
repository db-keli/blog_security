package org.example.blog_spring.web.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

import java.time.Instant;
import java.util.List;
import org.example.blog_spring.domain.PostStatus;
import org.example.blog_spring.dto.ApiResponse;
import org.example.blog_spring.dto.PostDto;
import org.example.blog_spring.dto.TagSummaryDto;
import org.example.blog_spring.service.PostService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class PostControllerTest {

    private final PostService postService = Mockito.mock(PostService.class);

    private final PostController controller = new PostController(postService);

    @Test
    void getPosts_returnsPagedPostsWrappedInApiResponse() {
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

        var responseEntity = controller.getPosts(null, null, null, null, PageRequest.of(0, 20));
        assertEquals(200, responseEntity.getStatusCode().value());

        ApiResponse<Page<PostDto>> body = responseEntity.getBody();
        assertEquals("Posts retrieved successfully", body.message());
        assertEquals("Title", body.data().getContent().getFirst().title());
    }
}

