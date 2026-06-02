# Store Management API

A REST API for managing a product catalog (CRUD), built with Spring Boot. Supports creating, reading, updating and deleting products, with role-based access control and request validation.

## Tech Stack

- Java 21
- Spring Boot 4
- Spring Data JPA + Hibernate
- H2 (in-memory database)
- Spring Security (JWT authentication)
- jjwt (JSON Web Token library)
- springdoc-openapi (Swagger UI)
- Maven

## How to Run

```bash
mvn spring-boot:run
```

The application starts on port `8080`.

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- H2 console (development only): http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:storedb`, user: `sa`, no password)

## Authentication

The API uses JWT (JSON Web Token) authentication. There are two in-memory users (demo only):

| Username | Password    | Role  | Access                   |
|----------|-------------|-------|--------------------------|
| `user`   | `userpass`  | USER  | GET                      |
| `admin`  | `adminpass` | ADMIN | GET, POST, PATCH, DELETE |

### Getting a token

Send credentials to the login endpoint:

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"adminpass"}'
```

The response contains a token:

```json
{"token":"eyJhbGciOiJIUzI1NiJ9..."}
```

### Using the token

Send the token in the `Authorization` header on every request:

```bash
curl http://localhost:8080/api/products/1 \
  -H "Authorization: Bearer <token>"
```

Tokens expire after 60 minutes (configurable via `jwt.expiration-minutes`).

In Swagger UI, use the **Authorize** button and paste the token (without the `Bearer ` prefix).

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

- **JWT authentication** for stateless, token-based access. After logging in once, the client sends a signed token on each request instead of credentials, so the server doesn't re-check the password on each request.
- **Stateless session** (`SessionCreationPolicy.STATELESS`): the server keeps no session; the token itself carries the user's identity and role.
- **Tokens signed with HMAC-SHA256** (HS256) using a secret key. The same key signs and verifies. In production the secret would not be committed - externalized via environment variable at minimum, ideally managed by a dedicated secrets manager.
- **Password hashing with BCrypt**. Users are in-memory for this demo; in production they would be stored in the database.

## Testing

```bash
mvn test
```

- Unit tests cover the service layer and JWT components (JUnit 5, Mockito, AssertJ).
- Controller tests (@WebMvcTest) cover the endpoints, role-based access control, input validation, and exception-to-status mapping.
- The `contextLoads` test verifies the full application context (including security configuration) starts without errors.