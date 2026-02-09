package org.example.blog_spring.mapper;

import org.example.blog_spring.domain.Comment;
import org.example.blog_spring.domain.Post;
import org.example.blog_spring.domain.User;
import org.example.blog_spring.dto.CommentDto;
import org.example.blog_spring.dto.CreateCommentRequest;
import org.example.blog_spring.dto.UpdateCommentRequest;

public final class CommentMapper {

    private CommentMapper() {}

    public static Comment toEntity(CreateCommentRequest request, Post post, User user,
            Comment parent) {
        return new Comment(post, user, request.content(), parent);
    }

    public static void updateEntity(Comment comment, UpdateCommentRequest request) {
        comment.setContent(request.content());
    }

    public static CommentDto toDto(Comment comment) {
        var parentId = comment.getParent() != null ? comment.getParent().getId() : null;

        return new CommentDto(comment.getId(), comment.getPost().getId(), comment.getUser().getId(),
                parentId, comment.getContent(), comment.getCreatedAt(), comment.getUpdatedAt());
    }
}

