package org.example.blog_spring.service;

import org.example.blog_spring.dto.CommentDto;
import org.example.blog_spring.dto.CreateCommentRequest;
import org.example.blog_spring.dto.UpdateCommentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {

    CommentDto createComment(CreateCommentRequest request);

    CommentDto getComment(Long id);

    Page<CommentDto> getCommentsForPost(Long postId, Pageable pageable);

    Page<CommentDto> getCommentsForUser(Long userId, Pageable pageable);

    CommentDto updateComment(Long id, UpdateCommentRequest request);

    void deleteComment(Long id);
}

