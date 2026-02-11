package org.example.blog_spring.service;

import org.example.blog_spring.dto.CreatePostRequest;
import org.example.blog_spring.dto.PostDto;
import org.example.blog_spring.dto.UpdatePostRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {

    PostDto createPost(CreatePostRequest request);

    PostDto getPost(Long id);

    PostDto getPostBySlug(String slug);

    /**
     * List posts with optional filters for author, tag, search term, and published-only flag.
     */
    Page<PostDto> getPosts(
            Long authorId,
            String tagSlug,
            String search,
            Boolean publishedOnly,
            Pageable pageable
    );

    PostDto updatePost(Long id, UpdatePostRequest request);

    void deletePost(Long id);
}

