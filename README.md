# Store Management API

A REST API for managing a product catalog (CRUD), built with Spring Boot. Supports creating, reading, updating and deleting products, with role-based access control and request validation.

## Tech Stack

- Java 21
- Spring Boot 4
- Spring Data JPA + Hibernate
- H2 (in-memory database)
- Spring Security (Basic Auth)
- springdoc-openapi (Swagger UI)
- Maven
- Bucket4j (rate limiting)

## How to Run

```bash
mvn spring-boot:run
```

The application starts on port `8080`.

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- H2 console (development only): http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:storedb`, user: `sa`, no password)

## Authentication

The API uses HTTP Basic Auth with two in-memory users (demo only):

| Username | Password    | Role  | Access                   |
|----------|-------------|-------|--------------------------|
| `user`   | `userpass`  | USER  | GET                      |
| `admin`  | `adminpass` | ADMIN | GET, POST, PATCH, DELETE |

In Swagger UI, use the **Authorize** button to provide credentials.

## API Endpoints

Base path: `/api/products`

| Method | Path                          | Description                 | Role  |
|--------|-------------------------------|-----------------------------|-------|
| POST   | `/api/products`               | Create a product            | ADMIN |
| GET    | `/api/products/{id}`          | Get a product by id         | USER  |
| GET    | `/api/products`               | List products (paginated)   | USER  |
| PATCH  | `/api/products/{id}/price`    | Update a product's price    | ADMIN |
| PATCH  | `/api/products/{id}/quantity` | Update a product's quantity | ADMIN |
| DELETE | `/api/products/{id}`          | Delete a product            | ADMIN |

Pagination on the list endpoint: `?page=0&size=10`.

## Rate Limiting

Requests are rate-limited per IP address using Bucket4j (token bucket algorithm). Each IP gets its own bucket; once it runs out, the API responds with `429 Too Many Requests` and a `ProblemDetail` body, consistent with the rest of the error handling.

The filter runs before Spring Security (`Ordered.HIGHEST_PRECEDENCE`), so every request is rate-limited regardless of authentication, including unauthenticated ones, which could otherwise flood the login endpoint.

| Property                      | Default | Description                      |
|-------------------------------|---------|----------------------------------|
| `rate-limit.capacity`         | 20      | max requests allowed per window  |
| `rate-limit.duration-minutes` | 1       | length of the window, in minutes |

With the defaults, each IP can make up to 20 requests per minute.

## Design Decisions

### Persistence

- **H2 in-memory database** for reproducibility and zero setup. In production I would use a persistent database (e.g. PostgreSQL) with schema migrations (Flyway).
- **H2 console enabled for local development** to inspect the database directly. It is permitted in the security chain only for convenience during development; in production it would be disabled, as it exposes direct database access.

### API & Validation

- **DTOs as records** (`ProductRequest` / `ProductResponse`) to decouple the API contract from the persistence model and to validate input at the boundary.
- **Pagination on the list endpoint** because returning an unbounded list does not scale with real data volumes.
- **Reject decimal values for integer fields** (Jackson `accept-float-as-int=false`): a value like `9.99` for quantity is rejected instead of being silently truncated to `9`, since silent data corruption is worse than a clear error.

### Error Handling

- **`ProblemDetail`** for error responses, giving a standardized format without a custom error DTO.
- **Centralized exception handling** in a `@RestControllerAdvice`, with logging: 4xx as warnings (client errors), 5xx as errors with full stack trace (real bugs).

### Security

- **Basic Auth** for simplicity, satisfying the requirement for a basic authentication mechanism. Without HTTPS, credentials are only Base64-encoded (not encrypted), so in production it would require HTTPS.
- **Password hashing with BCrypt**. Users are in-memory for this demo; in production they would be stored in the database.

## Testing

```bash
mvn test
```

Unit tests cover the service layer (JUnit 5, Mockito, AssertJ).
Controller tests (@WebMvcTest) cover the endpoints, role-based access control, input validation, and exception-to-status mapping.
The rate limiting filter is unit-tested for allowing requests within the limit, returning 429 when exceeded, and tracking IPs independently.
The `contextLoads` test verifies the full application context (including security configuration) starts without errors.