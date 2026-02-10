package org.example.blog_spring.service.impl;

import java.util.Set;

import org.example.blog_spring.domain.PostStatus;
import org.example.blog_spring.exception.PostNotFoundException;
import org.example.blog_spring.dto.CreatePostRequest;
import org.example.blog_spring.dto.PostDto;
import org.example.blog_spring.dto.UpdatePostRequest;
import org.example.blog_spring.mapper.PostMapper;
import org.example.blog_spring.repository.PostRepository;
import org.example.blog_spring.repository.TagRepository;
import org.example.blog_spring.repository.UserRepository;
import org.example.blog_spring.service.PostService;
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
        public PostDto createPost(CreatePostRequest request) {
                var author = userRepository.findById(request.authorId()).orElseThrow(
                                () -> new IllegalArgumentException("Author with id %d not found"
                                                .formatted(request.authorId())));

                Set<Long> tagIds = request.tagIds() != null ? request.tagIds() : Set.of();
                var tags = tagIds.isEmpty() ? Set.<org.example.blog_spring.domain.Tag>of()
                                : tagRepository.findByIdIn(tagIds);

                var post = PostMapper.toEntity(request, author, tags);
                var saved = postRepository.save(post);
                return PostMapper.toDto(saved);
        }

        @Override
        @Transactional(readOnly = true)
        public PostDto getPost(Long id) {
                var post = postRepository.findById(id)
                                .orElseThrow(() -> new PostNotFoundException(id));
                return PostMapper.toDto(post);
        }

        @Override
        @Transactional(readOnly = true)
        public PostDto getPostBySlug(String slug) {
                var post = postRepository.findBySlug(slug)
                                .orElseThrow(() -> new PostNotFoundException(slug));
                return PostMapper.toDto(post);
        }

        @Override
        @Transactional(readOnly = true)
        public Page<PostDto> getPosts(Long authorId, String tagSlug, String search,
                        Boolean publishedOnly, Pageable pageable) {
                PostStatus status = Boolean.TRUE.equals(publishedOnly) ? PostStatus.PUBLISHED : null;

                return postRepository.search(status, authorId, tagSlug, search, pageable)
                                .map(PostMapper::toDto);
        }

        @Override
        public PostDto updatePost(Long id, UpdatePostRequest request) {
                var post = postRepository.findById(id)
                                .orElseThrow(() -> new PostNotFoundException(id));

                Set<Long> tagIds = request.tagIds() != null ? request.tagIds() : Set.of();
                var tags = tagIds.isEmpty() ? Set.<org.example.blog_spring.domain.Tag>of()
                                : tagRepository.findByIdIn(tagIds);

                PostMapper.updateEntity(post, request, tags);
                var saved = postRepository.save(post);
                return PostMapper.toDto(saved);
        }

        @Override
        public void deletePost(Long id) {
                if (!postRepository.existsById(id)) {
                        throw new PostNotFoundException(id);
                }
                postRepository.deleteById(id);
        }
}

