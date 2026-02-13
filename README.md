# Blog Spring

A blog platform API built with Spring Boot, JDBC, REST, and GraphQL.

## Requirements

- Java 21
- PostgreSQL
- Maven

## Run

```bash
# Start PostgreSQL (Docker)
docker compose up -d

# Run the app
mvn spring-boot:run
```

Default: `http://localhost:8080`

## API

- REST: `/api/users`, `/api/posts`, `/api/tags`, `/api/comments`, `/api/reviews`
- GraphQL: `POST /graphql`
- Cache stats: `GET /api/cache/stats`
- OpenAPI: `/swagger-ui.html`

## Caching

Spring Cache is enabled via `@EnableCaching` on the main application class. Caches:

- `posts` – post by ID
- `postsBySlug` – post by slug
- `postLists` – paginated post listings
- `users` – user by ID
- `tags` – tag by ID and by slug

Cache is evicted on create/update/delete. To disable caching (e.g. for benchmarks):

```bash
mvn spring-boot:run -Dspring.cache.type=none
```

## Test

```bash
mvn test
```

Repository tests use Testcontainers and a shared PostgreSQL container. Ensure Docker is running.

To run benchmarks (baseline vs cached):

```bash
# Export DB vars in .env, then:
./scripts/run_benchmarks.sh
```

Reports are written to `metrics/`.

## Profiles

- `dev` – local development (default)
- `test` – tests
- `prod` – production

## Documentation

- [docs/REPOSITORY.md](docs/REPOSITORY.md) – repository structure and query logic
- [docs/TRANSACTIONS.md](docs/TRANSACTIONS.md) – transaction handling
