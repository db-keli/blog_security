package org.example.blog_spring.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.blog_spring.dto.ApiResponse;
import org.example.blog_spring.dto.CreateReviewRequest;
import org.example.blog_spring.dto.ReviewDto;
import org.example.blog_spring.dto.UpdateReviewRequest;
import org.example.blog_spring.service.ReviewService;
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
@RequestMapping("/api/reviews")
@Tag(name = "Reviews", description = "Review management APIs")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    @Operation(summary = "Create a new review")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Review created successfully",
            content = @Content(schema = @Schema(implementation = ReviewDto.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid input or validation failure"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Post or user not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Review already exists for this user and post"
        )
    })
    public ResponseEntity<ApiResponse<ReviewDto>> createReview(
            @Valid @RequestBody CreateReviewRequest request
    ) {
        var reviewDto = reviewService.createReview(request);
        var response =
                ApiResponse.success(HttpStatus.CREATED, "Review created successfully", reviewDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single review by id")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Review retrieved successfully",
            content = @Content(schema = @Schema(implementation = ReviewDto.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Review not found"
        )
    })
    public ResponseEntity<ApiResponse<ReviewDto>> getReview(@PathVariable Long id) {
        var reviewDto = reviewService.getReview(id);
        var response =
                ApiResponse.success(HttpStatus.OK, "Review retrieved successfully", reviewDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-user-and-post")
    @Operation(summary = "Get a review for a given user and post")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Review for user and post retrieved successfully",
            content = @Content(schema = @Schema(implementation = ReviewDto.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Review, user, or post not found"
        )
    })
    public ResponseEntity<ApiResponse<ReviewDto>> getReviewForUserAndPost(
            @RequestParam("userId") Long userId,
            @RequestParam("postId") Long postId
    ) {
        var reviewDto = reviewService.getReviewForUserAndPost(userId, postId);
        var response = ApiResponse.success(
                HttpStatus.OK,
                "Review for user and post retrieved successfully",
                reviewDto
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-post/{postId}")
    @Operation(summary = "List reviews for a post with pagination")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Reviews for post retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid pagination parameters"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Post not found"
        )
    })
    public ResponseEntity<ApiResponse<Page<ReviewDto>>> getReviewsForPost(
            @PathVariable Long postId,
            Pageable pageable
    ) {
        var reviews = reviewService.getReviewsForPost(postId, pageable);
        var response = ApiResponse.success(
                HttpStatus.OK,
                "Reviews for post retrieved successfully",
                reviews
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List reviews for a user with pagination")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Reviews for user retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid pagination parameters"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "User not found"
        )
    })
    public ResponseEntity<ApiResponse<Page<ReviewDto>>> getReviewsForUser(
            @RequestParam("userId") Long userId,
            Pageable pageable
    ) {
        var reviews = reviewService.getReviewsForUser(userId, pageable);
        var response = ApiResponse.success(
                HttpStatus.OK,
                "Reviews for user retrieved successfully",
                reviews
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing review")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Review updated successfully",
            content = @Content(schema = @Schema(implementation = ReviewDto.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid input or validation failure"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Review not found"
        )
    })
    public ResponseEntity<ApiResponse<ReviewDto>> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReviewRequest request
    ) {
        var updated = reviewService.updateReview(id, request);
        var response =
                ApiResponse.success(HttpStatus.OK, "Review updated successfully", updated);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a review")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "204",
            description = "Review deleted successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Review not found"
        )
    })
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        var response = ApiResponse.<Void>success(
                HttpStatus.NO_CONTENT,
                "Review deleted successfully",
                null
        );
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }
}

