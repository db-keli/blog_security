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

## Security

- Passwords are stored using `BCryptPasswordEncoder`.
- Authentication for APIs is token-based using JWT issued by `/api/auth/login`.
- Role model:
  - `ADMIN` – full access to admin endpoints (e.g. `/api/admin/**`) and content management.
  - `AUTHOR` – can create/update/delete posts.
  - `READER` – can read public content.

### CORS vs CSRF

- **CORS (Cross-Origin Resource Sharing)**:
  - Browser-side protection that controls which origins (`Origin` header) are allowed to call the API.
  - Configured globally in `SecurityConfig.corsConfigurationSource`.
  - Allowed origins (by default):
    - `http://localhost:3000` (React dev)
    - `http://localhost:5173` (Vite)
    - `http://localhost:8080` (other local clients)
  - Controls which methods and headers are allowed (e.g. `GET, POST, Authorization`).
  - Affects **browsers only**; tools like Postman ignore CORS.

- **CSRF (Cross-Site Request Forgery)**:
  - Protects state-changing requests in browser sessions (cookies).
  - For this application:
    - **Disabled for stateless JSON APIs**:
      - `/api/**`
      - `/graphql`
      - `/v3/api-docs/**`, `/swagger-ui/**`, `/actuator/**`
    - **Enabled for non-API paths**, with tokens stored in a cookie via `CookieCsrfTokenRepository`.
    - A demo controller `CsrfDemoController` exposes:
      - `GET /csrf-demo/token` – returns the current CSRF token (parameter name, header name, token value).
      - `POST /csrf-demo/submit` – example CSRF-protected endpoint.

#### When to enable CSRF

- **JWT / stateless APIs**:
  - CSRF is typically disabled because the browser does not automatically attach the bearer token; each request explicitly sends `Authorization: Bearer <jwt>`.
  - This project follows that pattern for `/api/**` and `/graphql`.

- **Form-based or cookie-based sessions**:
  - CSRF should be **enabled** and tokens must be sent with each modifying request (e.g. POST form submission).
  - For a traditional HTML form:
    - Include a hidden field with the CSRF token from `/csrf-demo/token`, or rely on a view engine (Thymeleaf) to inject it automatically.

### Practical testing

- **Postman**:
  - CORS is not enforced; you can call APIs directly.
  - To test JWT:
    - `POST /api/auth/login` to obtain `data.token`.
    - Use `Authorization: Bearer <token>` when calling protected endpoints like `/api/admin/health`.
  - To test CSRF demo:
    - `GET /csrf-demo/token` to see the CSRF token and header name.
    - `POST /csrf-demo/submit` with header, e.g. `X-XSRF-TOKEN: <token>` (depending on configured header name).

- **Browser frontend (React, JavaFX WebView, etc.)**:
  - Must be served from an allowed origin or CORS will block requests.
  - For JWT:
    - Store the token (e.g. in memory or secure storage) and send it as `Authorization: Bearer <token>` on each API call.
  - For CSRF-protected non-API endpoints:
    - Read the CSRF cookie and send the value in the expected header or form field when submitting forms.

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
- [docs/OPENAPI.md](docs/OPENAPI.md) – comprehensive OpenAPI/Swagger documentation
