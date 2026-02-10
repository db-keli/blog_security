## REST vs GraphQL Performance Report

### 1. Objectives

This report compares the performance of the **REST** and **GraphQL** APIs in the `blog_spring` application and evaluates API-level optimizations around **sorting, searching, and pagination**.

The primary questions:

- How does latency and throughput differ between REST and GraphQL for equivalent read operations?
- Are current query patterns and indexes sufficient for the expected data volume?
- Where should future optimization effort be focused?

---

### 2. Test Environment

- **Application**: `blog_spring` (Spring Boot 4.0.2, Java 21)
- **Database**: PostgreSQL 13+ (Docker, single instance)
- **Profiles**:
  - `dev` for local functional testing
  - `prod`-like configuration used for performance testing (no SQL logging, `ddl-auto=validate`)
- **Metrics**:
  - Spring Boot Actuator + Micrometer
  - Custom AOP metric: `blog.service.execution`
    - **Tags**:
      - `method`: short service method name (e.g. `PostServiceImpl.getPosts(..)`)
      - `outcome`: `success` / `error`

---

### 3. Scenarios Compared

The comparison focuses on **read-heavy** scenarios that stress sorting/searching/pagination, as defined in the project plan.

#### 3.1 List posts with filters (REST)

- Endpoint: `GET /api/posts`
- Query parameters:
  - `page=0&size=20`
  - `authorId=<id>`
  - `tag=<tag-slug>`
  - `search=<keyword>`
  - `publishedOnly=true`
- Service method: `PostServiceImpl.getPosts(authorId, tagSlug, search, publishedOnly, pageable)`
- Repository method: `PostRepository.search(...)` combining:
  - `status` filter
  - `authorId` filter
  - tag slug filter via `JOIN`
  - case-insensitive `LIKE` on `title` and `content`

#### 3.2 List posts with filters (GraphQL)

- Operation:

```graphql
query {
  posts(
    page: 0,
    size: 20,
    authorId: <id>,
    tag: "<tag-slug>",
    search: "<keyword>",
    publishedOnly: true
  ) {
    id
    title
    slug
    status
    createdAt
  }
}
```

- Controller: `BlogGraphQlController.posts(...)`
- Service method: **same** `PostServiceImpl.getPosts(...)` call as REST.

#### 3.3 Detail and child collections

Additional spot checks:

- `GET /api/posts/{id}` vs GraphQL `post(id: ID!)`
- `GET /api/comments/by-post/{postId}` vs GraphQL `commentsByPost(postId: ID!, ...)`
- `GET /api/reviews/by-post/{postId}` vs GraphQL `reviewsByPost(postId: ID!, ...)`

All use the same **service layer**, so performance differences mostly reflect transport and payload shape.

---

### 4. Methodology

1. **Warm-up**  
   - Run a series of REST and GraphQL calls to ensure the JVM, caches, and connection pools are warm.

2. **Load generation**  
   - Use a tool such as **k6**, **JMeter**, **Gatling**, or **Postman Collection Runner** to:
     - Execute the REST and GraphQL scenarios above under comparable load:
       - Fixed duration (e.g. 5 minutes per scenario)
       - Fixed concurrency (e.g. 10–50 virtual users)

3. **Metrics collection**  
   - During each scenario, periodically query:
     - `GET /actuator/metrics/blog.service.execution`
       - Filter by `method="PostServiceImpl.getPosts(..)"` and `outcome="success"`.
     - Read Micrometer statistics:
       - `count` (number of calls)
       - `totalTime`
       - `max`
       - `mean` / percentiles (if configured)

4. **Analysis**  
   - Compare:
     - **Average latency** per call (REST vs GraphQL) for the same service method.
     - **Throughput** (calls/second).
     - **Error rates** (if any, via `outcome="error"` tag).

---

### 5. Results (Example Interpretation)

> Note: Actual numbers will depend on data volume, hardware, and test harness. This section describes the **expected patterns** and how to interpret your own measurements.

#### 5.1 REST vs GraphQL list posts

Observed patterns in typical setups:

- **REST** `GET /api/posts`:
  - Slightly **lower overhead** per call for simple list responses.
  - Well-suited when the client always needs the same, fixed projection (`PostDto` with tags).

- **GraphQL** `posts(...)`:
  - Slightly higher overhead per request (GraphQL parsing, validation, and selection).
  - More efficient when the client:
    - Combines multiple data needs in one request (e.g. posts + comments + reviews).
    - Requests a **subset** of fields, reducing payload size.

With the **shared service layer** in this project, most of the cost is in:

- The `PostRepository.search(...)` query execution.
- JSON or GraphQL response serialization.

Where GraphQL shines is when:

- A single query replaces **multiple REST calls**, reducing overall round-trips and total latency from the client’s perspective.

#### 5.2 Comments and reviews

- Both REST and GraphQL use paginated queries for comments and reviews.
- Indexes on `post_id`, `user_id`, and `created_at` keep database lookups efficient.
- GraphQL allows clients to shape the review/comment fields they fetch, which can reduce payload size for analytics-style dashboards.

---

### 6. API-Level Optimizations

Based on the current design and expected results:

1. **Database indexes**
   - Indexes already exist on frequent filters:
     - Posts: `author_id`, `slug`, `status`, `created_at`, `published_at`
     - Tags: `name`, `slug`
     - Comments: `post_id`, `user_id`, `parent_id`, `created_at`
     - Reviews: `post_id`, `user_id`, `rating`
   - These indexes support the search query in `PostRepository.search(...)` and list operations.

2. **Efficient pagination**
   - All list endpoints (REST and GraphQL) use Spring Data **`Pageable`**.
   - For very deep paginations, consider:
     - Keyset pagination (using `id` or `created_at`) to avoid large `OFFSET`.

3. **Search strategy**
   - Current implementation uses `LIKE` on `title` and `content`.
   - For larger datasets, consider:
     - PostgreSQL full-text search (`tsvector`/`tsquery`) with appropriate indexes.
     - Expose search term through both REST and GraphQL.

4. **Selective field fetching (GraphQL)**
   - Encourage frontend to request only necessary fields.
   - This can significantly reduce bandwidth for heavy entities (e.g. large `content` fields).

5. **Service timing metrics**
   - Continue monitoring `blog.service.execution`:
     - Watch for methods with high `max` or p95/p99 latency.
     - Correlate spikes with specific queries or external dependencies.

---

### 7. Conclusions

- The **shared service and repository layers** ensure that REST and GraphQL are comparable at the business-logic and data-access level.
- **REST** tends to be simpler and slightly faster per call for fixed, well-known payloads.
- **GraphQL** provides better **flexibility and aggregate efficiency** (fewer round-trips) when multiple related resources or custom projections are needed.
- With proper indexing, pagination, and Micrometer‑backed AOP timing in place, the current design supports the project’s performance and scalability goals and can be tuned further as real-world usage patterns emerge.

