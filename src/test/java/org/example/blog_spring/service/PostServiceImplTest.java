package org.example.blog_spring.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.example.blog_spring.cache.CacheManager;
import org.example.blog_spring.domain.Post;
import org.example.blog_spring.domain.Tag;
import org.example.blog_spring.dto.CreatePostRequest;
import org.example.blog_spring.exception.PostNotFoundException;
import org.example.blog_spring.service.impl.PostServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock
    private org.example.blog_spring.repository.PostRepository postRepository;
    @Mock
    private org.example.blog_spring.repository.UserRepository userRepository;
    @Mock
    private org.example.blog_spring.repository.TagRepository tagRepository;
    @Mock
    private CacheManager cacheManager;

    private PostServiceImpl postService;

    @BeforeEach
    void setUp() {
        postService = new PostServiceImpl(postRepository, userRepository, tagRepository,
                cacheManager);
    }

    @Test
    void getPost_returnsCachedPost_whenInCache() {
        var post = Post.builder().id(1L).authorId(1L).title("Test").slug("test").tags(Set.of())
                .build();
        given(cacheManager.getPost(1L)).willReturn(post);

        var result = postService.getPost(1L);

        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("Test");
        verify(cacheManager).getPost(1L);
    }

    @Test
    void getPost_returnsFromDaoAndCaches_whenNotInCache() {
        var post = Post.builder().id(1L).authorId(1L).title("Test").slug("test").tags(Set.of())
                .build();
        given(cacheManager.getPost(1L)).willReturn(null);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        var result = postService.getPost(1L);

        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("Test");
        verify(postRepository).findById(1L);
        verify(cacheManager).putPost(post);
    }

    @Test
    void getPost_throws_whenNotFound() {
        given(cacheManager.getPost(999L)).willReturn(null);
        given(postRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPost(999L))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    void createPost_throws_whenAuthorNotFound() {
        given(userRepository.existsById(999L)).willReturn(false);
        var request = new CreatePostRequest(999L, "Title", "Content", "slug", Set.of());

        assertThatThrownBy(() -> postService.createPost(request))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("999");
    }

    @Test
    void createPost_savesAndCaches() {
        given(userRepository.existsById(1L)).willReturn(true);
        given(postRepository.save(any(Post.class))).willAnswer(inv -> {
            Post p = inv.getArgument(0);
            p.setId(1L);
            p.setTags(Set.of());
            return p;
        });

        var request = new CreatePostRequest(1L, "Title", "Content", "slug", Set.of());
        var result = postService.createPost(request);

        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("Title");
        verify(postRepository).save(any(Post.class));
        verify(cacheManager).putPost(any(Post.class));
        verify(cacheManager).invalidatePostListCache();
    }

    @Test
    void deletePost_throws_whenNotFound() {
        given(postRepository.existsById(999L)).willReturn(false);

        assertThatThrownBy(() -> postService.deletePost(999L))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    void deletePost_removesFromCache() {
        given(postRepository.existsById(1L)).willReturn(true);

        postService.deletePost(1L);

        verify(postRepository).deleteById(1L);
        verify(cacheManager).removePost(1L);
    }
}
