package org.example.blog_spring.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.blog_spring.dto.ApiResponse;
import org.example.blog_spring.dto.CommentDto;
import org.example.blog_spring.dto.CreateCommentRequest;
import org.example.blog_spring.dto.UpdateCommentRequest;
import org.example.blog_spring.service.CommentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
@Tag(name = "Comments", description = "Comment management APIs")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    @Operation(summary = "Create a new comment")
    public ResponseEntity<ApiResponse<CommentDto>> createComment(
            @Valid @RequestBody CreateCommentRequest request
    ) {
        var commentDto = commentService.createComment(request);
        var response =
                ApiResponse.success(HttpStatus.CREATED, "Comment created successfully", commentDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single comment by id")
    public ResponseEntity<ApiResponse<CommentDto>> getComment(@PathVariable Long id) {
        var commentDto = commentService.getComment(id);
        var response =
                ApiResponse.success(HttpStatus.OK, "Comment retrieved successfully", commentDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-post/{postId}")
    @Operation(summary = "List comments for a post with pagination")
    public ResponseEntity<ApiResponse<Page<CommentDto>>> getCommentsForPost(
            @PathVariable Long postId,
            Pageable pageable
    ) {
        var comments = commentService.getCommentsForPost(postId, pageable);
        var response = ApiResponse.success(
                HttpStatus.OK,
                "Comments for post retrieved successfully",
                comments
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List comments for a user with pagination")
    public ResponseEntity<ApiResponse<Page<CommentDto>>> getCommentsForUser(
            @RequestParam("userId") Long userId,
            Pageable pageable
    ) {
        var comments = commentService.getCommentsForUser(userId, pageable);
        var response = ApiResponse.success(
                HttpStatus.OK,
                "Comments for user retrieved successfully",
                comments
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing comment")
    public ResponseEntity<ApiResponse<CommentDto>> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCommentRequest request
    ) {
        var updated = commentService.updateComment(id, request);
        var response =
                ApiResponse.success(HttpStatus.OK, "Comment updated successfully", updated);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a comment")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        var response = ApiResponse.<Void>success(
                HttpStatus.NO_CONTENT,
                "Comment deleted successfully",
                null
        );
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }
}

