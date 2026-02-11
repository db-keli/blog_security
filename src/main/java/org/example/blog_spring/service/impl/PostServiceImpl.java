package org.example.blog_spring.service.impl;

import java.util.Set;

import org.example.blog_spring.cache.CacheManager;
import org.example.blog_spring.dao.PostDao;
import org.example.blog_spring.dao.TagDao;
import org.example.blog_spring.dao.UserDao;
import org.example.blog_spring.domain.Post;
import org.example.blog_spring.domain.PostStatus;
import org.example.blog_spring.dto.CreatePostRequest;
import org.example.blog_spring.dto.PostDto;
import org.example.blog_spring.dto.UpdatePostRequest;
import org.example.blog_spring.exception.PostNotFoundException;
import org.example.blog_spring.mapper.PostMapper;
import org.example.blog_spring.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PostServiceImpl implements PostService {

    private final PostDao postDao;
    private final UserDao userDao;
    private final TagDao tagDao;
    private final CacheManager cacheManager;

    public PostServiceImpl(PostDao postDao, UserDao userDao, TagDao tagDao, CacheManager cacheManager) {
        this.postDao = postDao;
        this.userDao = userDao;
        this.tagDao = tagDao;
        this.cacheManager = cacheManager;
    }

    @Override
    public PostDto createPost(CreatePostRequest request) {
        if (!userDao.existsById(request.authorId())) {
            throw new IllegalArgumentException("Author with id %d not found".formatted(request.authorId()));
        }
        Set<Long> tagIds = request.tagIds() != null ? request.tagIds() : Set.of();
        var tags = tagIds.isEmpty() ? Set.<org.example.blog_spring.domain.Tag>of()
                : Set.copyOf(tagDao.findByIdIn(tagIds));

        var post = PostMapper.toEntity(request, request.authorId(), tags);
        var saved = postDao.insert(post, tagIds);
        saved.setTags(tags);
        cacheManager.putPost(saved);
        cacheManager.invalidatePostListCache();
        return PostMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PostDto getPost(Long id) {
        var post = cacheManager.getPost(id);
        if (post == null) {
            post = postDao.findById(id).orElseThrow(() -> new PostNotFoundException(id));
            cacheManager.putPost(post);
        }
        return PostMapper.toDto(post);
    }

    @Override
    @Transactional(readOnly = true)
    public PostDto getPostBySlug(String slug) {
        var post = cacheManager.getPostBySlug(slug);
        if (post == null) {
            post = postDao.findBySlug(slug).orElseThrow(() -> new PostNotFoundException(slug));
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
            page = postDao.findAll(pageable);
        } else {
            page = postDao.search(status, authorId, tagSlug, search, pageable);
        }
        cacheManager.putPostList(cacheKey,
                new CacheManager.PageCacheEntry(page.getContent(), page.getTotalElements()));
        return page.map(PostMapper::toDto);
    }

    @Override
    public PostDto updatePost(Long id, UpdatePostRequest request) {
        var post = postDao.findById(id).orElseThrow(() -> new PostNotFoundException(id));

        Set<Long> tagIds = request.tagIds() != null ? request.tagIds() : Set.of();
        var tags = tagIds.isEmpty() ? Set.<org.example.blog_spring.domain.Tag>of()
                : Set.copyOf(tagDao.findByIdIn(tagIds));

        PostMapper.updateEntity(post, request, tags);
        postDao.update(post, tagIds);
        post.setTags(tags);
        cacheManager.putPost(post);
        cacheManager.invalidatePostListCache();
        return PostMapper.toDto(post);
    }

    @Override
    public void deletePost(Long id) {
        if (!postDao.existsById(id)) {
            throw new PostNotFoundException(id);
        }
        postDao.deleteById(id);
        cacheManager.removePost(id);
    }
}
