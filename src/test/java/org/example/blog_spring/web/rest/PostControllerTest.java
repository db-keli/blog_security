package org.example.blog_spring.web.rest;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import org.example.blog_spring.domain.PostStatus;
import org.example.blog_spring.dto.PostDto;
import org.example.blog_spring.dto.TagSummaryDto;
import org.example.blog_spring.service.PostService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @Test
    void getPosts_returnsPagedPostsWrappedInApiResponse() throws Exception {
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
                Mockito.any()
        )).willReturn(page);

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Posts retrieved successfully"))
                .andExpect(jsonPath("$.data.content[0].title").value("Title"));
    }
}

