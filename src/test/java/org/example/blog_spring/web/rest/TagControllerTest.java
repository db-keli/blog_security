package org.example.blog_spring.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.Instant;
import java.util.List;

import org.example.blog_spring.dto.ApiResponse;
import org.example.blog_spring.dto.TagDto;
import org.example.blog_spring.service.TagService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

class TagControllerTest {

    private final TagService tagService = Mockito.mock(TagService.class);
    private final TagController controller = new TagController(tagService);

    @Test
    void getTags_returnsPagedTagsWrappedInApiResponse() {
        var tag = new TagDto(1L, "Java", "java", "Java language", Instant.now());
        Page<TagDto> page = new PageImpl<>(List.of(tag), PageRequest.of(0, 20), 1);

        given(tagService.getTags(Mockito.any(Pageable.class))).willReturn(page);

        ResponseEntity<ApiResponse<Page<TagDto>>> response = controller.getTags(PageRequest.of(0, 20));

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Tags retrieved successfully");
        assertThat(response.getBody().data().getContent().getFirst().name()).isEqualTo("Java");
    }
}
