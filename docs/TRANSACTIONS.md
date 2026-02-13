# Transaction Handling

## Strategy

All service implementations use `@Transactional` at class level. Write operations use the default (read-write) transaction. Read-only operations use `@Transactional(readOnly = true)` for optimization.

## Per-Service

- **PostServiceImpl**: Class-level `@Transactional`. Read methods (`getPost`, `getPostBySlug`, `getPosts`) use `readOnly = true`.
- **UserServiceImpl**: Same pattern.
- **TagServiceImpl**: Same pattern.
- **ReviewServiceImpl**: Same pattern.
- **CommentServiceImpl**: Same pattern.

## Atomic Operations

### Comment Count Update

When a comment is created or deleted, the post's `comment_count` is updated in the same transaction:

1. Save or delete the comment
2. Recompute count via `commentRepository.countByPostId(postId)`
3. Update post and save

This keeps `comment_count` consistent without a separate background job. The entire flow runs in one transaction; on rollback, both the comment change and the count update are reverted.

### DatabaseSeeder

Uses `@Transactional` on its `run` method so the full seed runs atomically.
