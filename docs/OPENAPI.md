# OpenAPI/Swagger Documentation

## Overview

This document describes the OpenAPI/Swagger documentation implementation for the Blog Spring API. All REST endpoints are comprehensively documented with detailed response specifications.

## Implementation

### Annotations Used

- `@Tag`: Groups related endpoints together in the Swagger UI
- `@Operation`: Provides a summary description for each endpoint
- `@ApiResponses`: Container for multiple `@ApiResponse` annotations
- `@ApiResponse`: Documents a single HTTP response with status code, description, and schema
- `@Content`: Specifies the response content type and schema
- `@Schema`: References the DTO class used in the response

### API Groups

The API is organized into the following groups:

1. **Users** - User management endpoints
2. **Posts** - Blog post management endpoints
3. **Tags** - Tag management endpoints
4. **Comments** - Comment management endpoints
5. **Reviews** - Review management endpoints
6. **Cache** - Cache statistics and management endpoints

## HTTP Response Codes

### Success Responses

- **200 OK**: Resource retrieved or action completed successfully
- **201 CREATED**: New resource created successfully
- **204 NO CONTENT**: Resource deleted successfully

### Client Error Responses

- **400 BAD REQUEST**: Invalid input, validation failure, or invalid pagination parameters
- **404 NOT FOUND**: Requested resource not found
- **409 CONFLICT**: Resource already exists (duplicate email, username, slug, etc.)

### Server Error Responses

- **500 INTERNAL SERVER ERROR**: Unexpected server error (handled globally)

## Endpoint Documentation Examples

### User Endpoints

#### POST /api/users
- **201**: User created successfully (returns `UserDto`)
- **400**: Invalid input or validation failure
- **409**: Email or username already exists

#### GET /api/users/{id}
- **200**: User retrieved successfully (returns `UserDto`)
- **404**: User not found

#### GET /api/users
- **200**: Users retrieved successfully (returns `Page<UserDto>`)
- **400**: Invalid pagination parameters

#### PUT /api/users/{id}
- **200**: User updated successfully (returns `UserDto`)
- **400**: Invalid input or validation failure
- **404**: User not found
- **409**: Email or username already exists

#### DELETE /api/users/{id}
- **204**: User deleted successfully
- **404**: User not found

### Post Endpoints

#### POST /api/posts
- **201**: Post created successfully (returns `PostDto`)
- **400**: Invalid input, validation failure, or author not found
- **409**: Post with the same slug already exists

#### GET /api/posts/{id}
- **200**: Post retrieved successfully (returns `PostDto`)
- **404**: Post not found

#### GET /api/posts/slug/{slug}
- **200**: Post retrieved successfully (returns `PostDto`)
- **404**: Post not found

#### GET /api/posts
- **200**: Posts retrieved successfully (returns `Page<PostDto>`)
- **400**: Invalid pagination or filter parameters

Supports optional filters:
- `authorId`: Filter by author ID
- `tag`: Filter by tag slug
- `search`: Full-text search in title and content
- `publishedOnly`: Filter by published status

#### PUT /api/posts/{id}
- **200**: Post updated successfully (returns `PostDto`)
- **400**: Invalid input or validation failure
- **404**: Post not found
- **409**: Post with the same slug already exists

#### DELETE /api/posts/{id}
- **204**: Post deleted successfully
- **404**: Post not found

### Tag Endpoints

#### POST /api/tags
- **201**: Tag created successfully (returns `TagDto`)
- **400**: Invalid input or validation failure
- **409**: Tag with the same name or slug already exists

#### GET /api/tags/{id}
- **200**: Tag retrieved successfully (returns `TagDto`)
- **404**: Tag not found

#### GET /api/tags/slug/{slug}
- **200**: Tag retrieved successfully (returns `TagDto`)
- **404**: Tag not found

#### GET /api/tags
- **200**: Tags retrieved successfully (returns `Page<TagDto>`)
- **400**: Invalid pagination parameters

#### PUT /api/tags/{id}
- **200**: Tag updated successfully (returns `TagDto`)
- **400**: Invalid input or validation failure
- **404**: Tag not found
- **409**: Tag with the same name or slug already exists

#### DELETE /api/tags/{id}
- **204**: Tag deleted successfully
- **404**: Tag not found

### Comment Endpoints

#### POST /api/comments
- **201**: Comment created successfully (returns `CommentDto`)
- **400**: Invalid input or validation failure
- **404**: Post or user not found

#### GET /api/comments/{id}
- **200**: Comment retrieved successfully (returns `CommentDto`)
- **404**: Comment not found

#### GET /api/comments/by-post/{postId}
- **200**: Comments for post retrieved successfully (returns `Page<CommentDto>`)
- **400**: Invalid pagination parameters
- **404**: Post not found

#### GET /api/comments?userId={userId}
- **200**: Comments for user retrieved successfully (returns `Page<CommentDto>`)
- **400**: Invalid pagination parameters
- **404**: User not found

#### PUT /api/comments/{id}
- **200**: Comment updated successfully (returns `CommentDto`)
- **400**: Invalid input or validation failure
- **404**: Comment not found

#### DELETE /api/comments/{id}
- **204**: Comment deleted successfully
- **404**: Comment not found

### Review Endpoints

#### POST /api/reviews
- **201**: Review created successfully (returns `ReviewDto`)
- **400**: Invalid input or validation failure
- **404**: Post or user not found
- **409**: Review already exists for this user and post

#### GET /api/reviews/{id}
- **200**: Review retrieved successfully (returns `ReviewDto`)
- **404**: Review not found

#### GET /api/reviews/by-user-and-post?userId={userId}&postId={postId}
- **200**: Review for user and post retrieved successfully (returns `ReviewDto`)
- **404**: Review, user, or post not found

#### GET /api/reviews/by-post/{postId}
- **200**: Reviews for post retrieved successfully (returns `Page<ReviewDto>`)
- **400**: Invalid pagination parameters
- **404**: Post not found

#### GET /api/reviews?userId={userId}
- **200**: Reviews for user retrieved successfully (returns `Page<ReviewDto>`)
- **400**: Invalid pagination parameters
- **404**: User not found

#### PUT /api/reviews/{id}
- **200**: Review updated successfully (returns `ReviewDto`)
- **400**: Invalid input or validation failure
- **404**: Review not found

#### DELETE /api/reviews/{id}
- **204**: Review deleted successfully
- **404**: Review not found

### Cache Endpoints

#### GET /api/cache/stats
- **200**: Cache statistics retrieved successfully

Returns legacy cache statistics including:
- Post cache size
- Post slug cache size
- Tag cache size
- Post list cache size
- Cache hits and misses
- Hit rate

#### POST /api/cache/clear
- **200**: Cache cleared successfully

Clears all legacy caches. Note: This does not affect Spring Cache.

## Global Exception Handling

All error responses follow a consistent structure through the `GlobalExceptionHandler`:

```json
{
  "status": 400,
  "message": "Validation failed",
  "data": {
    "fieldName": "error message"
  }
}
```

Success responses follow this structure:

```json
{
  "status": 200,
  "message": "Resource retrieved successfully",
  "data": { ... }
}
```

## Accessing Swagger UI

When running the application, access the Swagger UI at:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **OpenAPI YAML**: http://localhost:8080/v3/api-docs.yaml

## Benefits

1. **Clear Documentation**: Every endpoint is documented with all possible response codes
2. **Interactive Testing**: Swagger UI allows direct API testing from the browser
3. **Type Safety**: Response schemas reference actual DTO classes
4. **Consistent Structure**: All endpoints follow the same documentation pattern
5. **Client Generation**: OpenAPI spec can be used to generate client libraries
6. **Error Clarity**: All error scenarios are explicitly documented

## Validation

All request bodies use Jakarta Bean Validation annotations (`@Valid`) which are automatically documented in the OpenAPI specification.

Common validation constraints:
- `@NotNull`: Field is required
- `@NotBlank`: String field is required and non-empty
- `@Size`: String or collection size constraints
- `@Email`: Valid email format
- `@Min/@Max`: Numeric range constraints
