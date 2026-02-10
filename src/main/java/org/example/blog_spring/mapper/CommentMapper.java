package org.example.blog_spring.mapper;

import org.example.blog_spring.domain.Comment;
import org.example.blog_spring.dto.CommentDto;
import org.example.blog_spring.dto.CreateCommentRequest;
import org.example.blog_spring.dto.UpdateCommentRequest;

public final class CommentMapper {

    private CommentMapper() {}

    public static Comment toEntity(CreateCommentRequest request) {
        return new Comment(request.postId(), request.userId(), request.content(), request.parentId());
    }

    public static void updateEntity(Comment comment, UpdateCommentRequest request) {
        comment.setContent(request.content());
        comment.setUpdatedAt(java.time.Instant.now());
    }

    public static CommentDto toDto(Comment comment) {
        return new CommentDto(comment.getId(), comment.getPostId(), comment.getUserId(),
                comment.getParentId(), comment.getContent(), comment.getCreatedAt(), comment.getUpdatedAt());
    }
}
