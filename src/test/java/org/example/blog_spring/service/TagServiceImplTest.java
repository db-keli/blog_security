package org.example.blog_spring.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.example.blog_spring.cache.CacheManager;
import org.example.blog_spring.domain.Tag;
import org.example.blog_spring.dto.CreateTagRequest;
import org.example.blog_spring.dto.UpdateTagRequest;
import org.example.blog_spring.exception.TagNotFoundException;
import org.example.blog_spring.service.impl.TagServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {

    @Mock
    private org.example.blog_spring.repository.TagRepository tagRepository;
    @Mock
    private CacheManager cacheManager;

    private TagServiceImpl tagService;

    @BeforeEach
    void setUp() {
        tagService = new TagServiceImpl(tagRepository, cacheManager);
    }

    @Test
    void getTag_returnsCachedTag_whenInCache() {
        var tag = Tag.builder().id(1L).name("Java").slug("java").build();
        given(cacheManager.getTag(1L)).willReturn(tag);

        var result = tagService.getTag(1L);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Java");
    }

    @Test
    void getTag_returnsFromDaoAndCaches_whenNotInCache() {
        var tag = Tag.builder().id(1L).name("Java").slug("java").build();
        given(cacheManager.getTag(1L)).willReturn(null);
        given(tagRepository.findById(1L)).willReturn(Optional.of(tag));

        var result = tagService.getTag(1L);

        assertThat(result).isNotNull();
        verify(cacheManager).putTag(tag);
    }

    @Test
    void getTag_throws_whenNotFound() {
        given(cacheManager.getTag(999L)).willReturn(null);
        given(tagRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> tagService.getTag(999L)).isInstanceOf(TagNotFoundException.class);
    }

    @Test
    void createTag_throws_whenNameExists() {
        given(tagRepository.existsByName("Java")).willReturn(true);
        var request = new CreateTagRequest("Java", "java", null);

        assertThatThrownBy(() -> tagService.createTag(request))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Java");
    }

    @Test
    void createTag_throws_whenSlugExists() {
        given(tagRepository.existsByName("New")).willReturn(false);
        given(tagRepository.existsBySlug("existing")).willReturn(true);
        var request = new CreateTagRequest("New", "existing", null);

        assertThatThrownBy(() -> tagService.createTag(request))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("existing");
    }

    @Test
    void createTag_savesAndCaches() {
        given(tagRepository.existsByName("New")).willReturn(false);
        given(tagRepository.existsBySlug("new")).willReturn(false);
        given(tagRepository.save(any(Tag.class))).willAnswer(inv -> {
            Tag t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        var request = new CreateTagRequest("New", "new", "Desc");
        var result = tagService.createTag(request);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("New");
        verify(cacheManager).putTag(any(Tag.class));
    }
}
