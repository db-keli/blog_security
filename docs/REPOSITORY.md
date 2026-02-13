# Repository Structure and Query Logic

## Repositories

| Repository | Entity | Purpose |
|------------|--------|---------|
| UserRepository | User | Users, auth lookups |
| PostRepository | Post | Posts, filtered listings |
| TagRepository | Tag | Tags, slug lookups |
| CommentRepository | Comment | Comments by post or user |
| ReviewRepository | Review | Reviews by post or user |

## Query Logic

### UserRepository
- `findAll(Pageable)` - paginated users
- `findByEmail(String)` - lookup by email
- `existsByEmail`, `existsByUsername` - uniqueness checks

### PostRepository
- `findBySlug(String)` - single post by slug
- `findByStatus(PostStatus, Pageable)` - posts by status (DRAFT, PUBLISHED)
- `findByAuthorId(Long, Pageable)` - posts by author
- `findByTagSlug(String, Pageable)` - posts with a given tag (JOIN post_tags)
- `search(status, authorId, tagSlug, search, Pageable)` - combined filter: status, author, tag, and title/content search. Uses LOWER(CAST(...)) for PostgreSQL compatibility.

### TagRepository
- `findBySlug(String)` - single tag by slug
- `findAll(Pageable)` - paginated tags
- `findByIdIn(Set<Long>)` - batch fetch by IDs
- `existsByName`, `existsBySlug` - uniqueness checks

### CommentRepository
- `findByPostId(Long, Pageable)` - comments for a post
- `findByUserId(Long, Pageable)` - comments by a user
- `countByPostId(Long)` - comment count for a post (used for denormalized comment_count on Post)

### ReviewRepository
- `findByPostIdAndUserId(Long, Long)` - single review for post+user
- `findByPostId(Long, Pageable)` - reviews for a post
- `findByUserId(Long, Pageable)` - reviews by a user
