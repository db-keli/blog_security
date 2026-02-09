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

    Page<PostDto> getPosts(Pageable pageable);

    Page<PostDto> getPublishedPosts(Pageable pageable);

    PostDto updatePost(Long id, UpdatePostRequest request);

    void deletePost(Long id);
}

