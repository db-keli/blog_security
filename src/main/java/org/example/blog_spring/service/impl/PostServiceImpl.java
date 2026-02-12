package org.example.blog_spring.service.impl;

import java.util.Set;

import org.example.blog_spring.cache.CacheManager;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final CacheManager cacheManager;

    public PostServiceImpl(PostRepository postRepository, UserRepository userRepository,
            TagRepository tagRepository, CacheManager cacheManager) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.cacheManager = cacheManager;
    }

    @Override
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
        cacheManager.putPost(saved);
        cacheManager.invalidatePostListCache();
        return PostMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PostDto getPost(Long id) {
        var post = cacheManager.getPost(id);
        if (post == null) {
            post = postRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id));
            cacheManager.putPost(post);
        }
        return PostMapper.toDto(post);
    }

    @Override
    @Transactional(readOnly = true)
    public PostDto getPostBySlug(String slug) {
        var post = cacheManager.getPostBySlug(slug);
        if (post == null) {
            post = postRepository.findBySlug(slug)
                    .orElseThrow(() -> new PostNotFoundException(slug));
            cacheManager.putPost(post);
        }
        return PostMapper.toDto(post);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostDto> getPosts(Long authorId, String tagSlug, String search,
            Boolean publishedOnly, Pageable pageable) {
        PostStatus status = Boolean.TRUE.equals(publishedOnly) ? PostStatus.PUBLISHED : null;
        var cacheKey = CacheManager.buildPostListKey(authorId, tagSlug, search, status,
                pageable.getPageNumber(), pageable.getPageSize());

        var cached = cacheManager.getPostList(cacheKey);
        if (cached != null) {
            return new PageImpl<>(cached.content().stream().map(PostMapper::toDto).toList(),
                    pageable, cached.totalElements());
        }

        Page<Post> page;
        if (status == null && authorId == null && tagSlug == null && search == null) {
            page = postRepository.findAll(pageable);
        } else {
            page = postRepository.search(status, authorId, tagSlug, search, pageable);
        }
        cacheManager.putPostList(cacheKey,
                new CacheManager.PageCacheEntry(page.getContent(), page.getTotalElements()));
        return page.map(PostMapper::toDto);
    }

    @Override
    public PostDto updatePost(Long id, UpdatePostRequest request) {
        var post = postRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id));

        Set<Long> tagIds = request.tagIds() != null ? request.tagIds() : Set.of();
        Set<Tag> tags = tagIds.isEmpty() ? Set.of()
                : Set.copyOf(tagRepository.findByIdIn(tagIds));

        PostMapper.updateEntity(post, request, tags);
        post.setTags(tags);
        var saved = postRepository.save(post);
        cacheManager.putPost(saved);
        cacheManager.invalidatePostListCache();
        return PostMapper.toDto(saved);
    }

    @Override
    public void deletePost(Long id) {
        if (!postRepository.existsById(id)) {
            throw new PostNotFoundException(id);
        }
        postRepository.deleteById(id);
        cacheManager.removePost(id);
    }
}
