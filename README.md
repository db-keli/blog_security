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

## Test

```bash
mvn test
```

## Profiles

- `dev` – local development (default)
- `test` – tests
- `prod` – production
