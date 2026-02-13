package org.example.blog_spring.service.impl;

import java.util.Set;

import org.example.blog_spring.domain.Post;
import org.example.blog_spring.domain.PostStatus;
import org.example.blog_spring.domain.Tag;
import org.example.blog_spring.dto.CreatePostRequest;
import org.example.blog_spring.dto.PostDto;
import org.example.blog_spring.dto.UpdatePostRequest;
import org.example.blog_spring.exception.PostNotFoundException;
import org.example.blog_spring.mapper.PostMapper;
import org.example.blog_spring.repository.PostRepository;
import org.example.blog_spring.repository.TagRepository;
import org.example.blog_spring.repository.UserRepository;
import org.example.blog_spring.service.PostService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    public PostServiceImpl(PostRepository postRepository, UserRepository userRepository,
            TagRepository tagRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "posts", key = "#result.id()", condition = "#result != null"),
            @CacheEvict(cacheNames = "postsBySlug", key = "#result.slug()",
                    condition = "#result != null"),
            @CacheEvict(cacheNames = "postLists", allEntries = true)
    })
    public PostDto createPost(CreatePostRequest request) {
        if (!userRepository.existsById(request.authorId())) {
            throw new IllegalArgumentException(
                    "Author with id %d not found".formatted(request.authorId()));
        }
        Set<Long> tagIds = request.tagIds() != null ? request.tagIds() : Set.of();
        Set<Tag> tags = tagIds.isEmpty() ? Set.of()
                : Set.copyOf(tagRepository.findByIdIn(tagIds));

        var post = PostMapper.toEntity(request, request.authorId(), tags);
        post.setTags(tags);
        var saved = postRepository.save(post);
        return PostMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "posts", key = "#id")
    public PostDto getPost(Long id) {
        var post = postRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id));
        return PostMapper.toDto(post);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "postsBySlug", key = "#slug")
    public PostDto getPostBySlug(String slug) {
        var post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new PostNotFoundException(slug));
        return PostMapper.toDto(post);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "postLists",
            key = "T(java.util.Objects).hash(#authorId, #tagSlug, #search, #publishedOnly, #pageable.pageNumber, #pageable.pageSize)")
    public Page<PostDto> getPosts(Long authorId, String tagSlug, String search,
            Boolean publishedOnly, Pageable pageable) {
        PostStatus status = Boolean.TRUE.equals(publishedOnly) ? PostStatus.PUBLISHED : null;

        Page<Post> page;
        // Prefer simpler repository methods for common single-filter cases
        if (search != null && !search.isBlank()) {
            page = postRepository.search(status, authorId, tagSlug, search, pageable);
        } else if (status != null && authorId == null && tagSlug == null) {
            page = postRepository.findByStatus(status, pageable);
        } else if (authorId != null && status == null && tagSlug == null) {
            page = postRepository.findByAuthorId(authorId, pageable);
        } else if (tagSlug != null && status == null && authorId == null) {
            page = postRepository.findByTagSlug(tagSlug, pageable);
        } else if (status == null && authorId == null && tagSlug == null && search == null) {
            page = postRepository.findAll(pageable);
        } else {
            page = postRepository.search(status, authorId, tagSlug, search, pageable);
        }
        return page.map(PostMapper::toDto);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "posts", key = "#id"),
            @CacheEvict(cacheNames = "postsBySlug", allEntries = true),
            @CacheEvict(cacheNames = "postLists", allEntries = true)
    })
    public PostDto updatePost(Long id, UpdatePostRequest request) {
        var post = postRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id));

        Set<Long> tagIds = request.tagIds() != null ? request.tagIds() : Set.of();
        Set<Tag> tags = tagIds.isEmpty() ? Set.of()
                : Set.copyOf(tagRepository.findByIdIn(tagIds));

        PostMapper.updateEntity(post, request, tags);
        post.setTags(tags);
        var saved = postRepository.save(post);
        return PostMapper.toDto(saved);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "posts", key = "#id"),
            @CacheEvict(cacheNames = "postsBySlug", allEntries = true),
            @CacheEvict(cacheNames = "postLists", allEntries = true)
    })
    public void deletePost(Long id) {
        if (!postRepository.existsById(id)) {
            throw new PostNotFoundException(id);
        }
        postRepository.deleteById(id);
    }
}
