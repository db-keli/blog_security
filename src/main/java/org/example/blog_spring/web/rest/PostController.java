package org.example.blog_spring.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.blog_spring.dto.ApiResponse;
import org.example.blog_spring.dto.CreatePostRequest;
import org.example.blog_spring.dto.PostDto;
import org.example.blog_spring.dto.UpdatePostRequest;
import org.example.blog_spring.service.PostService;
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
@RequestMapping("/api/posts")
@Tag(name = "Posts", description = "Post management APIs")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    @Operation(summary = "Create a new post")
    public ResponseEntity<ApiResponse<PostDto>> createPost(
            @Valid @RequestBody CreatePostRequest request
    ) {
        var postDto = postService.createPost(request);
        var response =
                ApiResponse.success(HttpStatus.CREATED, "Post created successfully", postDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single post by id")
    public ResponseEntity<ApiResponse<PostDto>> getPost(@PathVariable Long id) {
        var postDto = postService.getPost(id);
        var response = ApiResponse.success(HttpStatus.OK, "Post retrieved successfully", postDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get a single post by slug")
    public ResponseEntity<ApiResponse<PostDto>> getPostBySlug(@PathVariable String slug) {
        var postDto = postService.getPostBySlug(slug);
        var response =
                ApiResponse.success(HttpStatus.OK, "Post retrieved successfully", postDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List posts with pagination")
    public ResponseEntity<ApiResponse<Page<PostDto>>> getPosts(
            @RequestParam(name = "publishedOnly", defaultValue = "false") boolean publishedOnly,
            Pageable pageable
    ) {
        Page<PostDto> posts =
                publishedOnly ? postService.getPublishedPosts(pageable)
                        : postService.getPosts(pageable);
        var response =
                ApiResponse.success(HttpStatus.OK, "Posts retrieved successfully", posts);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing post")
    public ResponseEntity<ApiResponse<PostDto>> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePostRequest request
    ) {
        var updated = postService.updatePost(id, request);
        var response =
                ApiResponse.success(HttpStatus.OK, "Post updated successfully", updated);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a post")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        var response =
                ApiResponse.<Void>success(HttpStatus.NO_CONTENT, "Post deleted successfully", null);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }
}

