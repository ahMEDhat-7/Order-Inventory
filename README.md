# Order & Inventory Processing Service

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 4.1.1](https://img.shields.io/badge/Spring%20Boot-4.1.1-green.svg)](https://spring.io/projects/spring-boot)
[![CI Pipeline](https://github.com/ahMEDhat-7/Order-Inventory/actions/workflows/ci.yml/badge.svg)](https://github.com/ahMEDhat-7/Order-Inventory/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

An enterprise-grade backend microservice for managing product inventory, processing customer orders, and maintaining transactional data integrity under concurrent traffic. Built with layered architecture, robust security, and comprehensive testing.

---

## Architecture

The system implements a classic **Layered Architecture (N-Tier)**:

```
[ HTTP Requests / API Clients ]
          │
          ▼
┌──────────────────┐
│ Controller Layer │  REST Endpoints, DTO Validation, Swagger
└─────────┬────────┘
          │
          ▼
┌──────────────────┐
│  Service Layer   │  Business Logic, Transactions, Caching
└─────────┬────────┘
          │
          ▼
┌──────────────────┐
│ Repository Layer │  Spring Data JPA, Hibernate
└─────────┬────────┘
          │
          ▼
┌──────────────────┐
│  Cache / DB      │  Redis Caching, PostgreSQL
└──────────────────┘
```

**Core Principles:**
- Stateless application design with JWT token verification
- Role-Based Access Control (RBAC) enforced via Spring Security filters
- `@Transactional` boundaries with optimistic locking (`@Version`) for concurrency
- Constructor-based Dependency Injection via Lombok `@RequiredArgsConstructor`

---

## Tech Stack

| Category | Technology | Version |
|:---|:---|:---|
| **Language** | Java | 21 |
| **Framework** | Spring Boot | 4.1.1 |
| **Security** | Spring Security + JJWT | 0.12.6 |
| **ORM** | Spring Data JPA + Hibernate | — |
| **Database** | PostgreSQL | 18-alpine |
| **Caching** | Redis | 8-alpine |
| **Migrations** | Flyway | — |
| **Validation** | Jakarta Bean Validation | — |
| **API Docs** | Springdoc OpenAPI (Swagger UI) | 3.1.0 |
| **Boilerplate** | Lombok | — |
| **Unit Testing** | JUnit 5 + Mockito | — |
| **Integration Testing** | Testcontainers | 1.20.4 |
| **Build** | Maven | 3.9.16 |
| **Containerization** | Docker + Docker Compose | — |
| **CI/CD** | GitHub Actions | — |

---

## Features

- **JWT Authentication** — Stateless token-based auth with BCrypt password hashing
- **Role-Based Access Control** — `ROLE_USER` (browse, order) and `ROLE_ADMIN` (manage products)
- **Product CRUD** — Full catalog management with admin-only create/update/delete
- **Optimistic Locking** — `@Version` on `Product` prevents race conditions on stock updates
- **Atomic Order Processing** — `@Transactional` stock deduction with automatic rollback on failure
- **Redis Caching** — Product reads cached under `product:{id}` with 30-minute TTL
- **Cache Eviction** — Automatic cache invalidation on product update/delete/stock change
- **Flyway Migrations** — Versioned SQL schema management (`V1__init_schema.sql`)
- **Global Exception Handling** — `@RestControllerAdvice` with structured error responses
- **Swagger UI** — Interactive API documentation at `/swagger-ui.html`
- **Docker** — Multi-stage Dockerfile for API image, docker compose for full stack
- **CI/CD** — GitHub Actions pipeline: compile → test → package

---

## Project Structure

```
inventory/
├── .github/
│   └── workflows/
│       └── ci.yml                          # GitHub Actions CI pipeline
├── docker compose.yml                      # PostgreSQL + Redis + App orchestration
├── Dockerfile                              # Multi-stage build for API
├── pom.xml                                 # Maven dependencies and plugins
└── src/
    ├── main/
    │   ├── java/com/enterprise/inventory/
    │   │   ├── InventoryApplication.java   # Spring Boot entry point
    │   │   ├── config/
    │   │   │   ├── RedisConfig.java        # RedisTemplate configuration
    │   │   │   ├── SecurityConfig.java     # Spring Security + JWT filter chain
    │   │   │   └── SwaggerConfig.java      # OpenAPI documentation config
    │   │   ├── controller/
    │   │   │   ├── AuthController.java     # POST /auth/register, /auth/login
    │   │   │   ├── ProductController.java  # CRUD /products
    │   │   │   └── OrderController.java    # POST /orders, GET /orders
    │   │   ├── dto/
    │   │   │   ├── request/
    │   │   │   │   ├── LoginRequest.java
    │   │   │   │   ├── OrderRequest.java
    │   │   │   │   ├── ProductRequest.java
    │   │   │   │   └── RegisterRequest.java
    │   │   │   └── response/
    │   │   │       ├── AuthResponse.java
    │   │   │       ├── OrderResponse.java
    │   │   │       └── ProductResponse.java
    │   │   ├── exception/
    │   │   │   ├── GlobalExceptionHandler.java
    │   │   │   └── InsufficientStockException.java
    │   │   ├── model/
    │   │   │   ├── Order.java
    │   │   │   ├── OrderItem.java
    │   │   │   ├── Product.java
    │   │   │   └── User.java
    │   │   ├── repository/
    │   │   │   ├── OrderRepository.java
    │   │   │   ├── ProductRepository.java
    │   │   │   └── UserRepository.java
    │   │   ├── security/
    │   │   │   ├── JwtAuthenticationFilter.java
    │   │   │   └── JwtUtil.java
    │   │   └── service/
    │   │       ├── AuthService.java
    │   │       ├── OrderService.java
    │   │       └── ProductService.java
    │   └── resources/
    │       ├── application.yml
    │       └── db/migration/
    │           └── V1__init_schema.sql     # Flyway migration
    └── test/
        └── java/com/enterprise/inventory/
            ├── AbstractIntegrationTest.java # Testcontainers base class
            ├── InventoryApplicationTests.java
            └── service/
                ├── AuthServiceTest.java
                ├── OrderServiceTest.java
                └── ProductServiceTest.java
```

---

## Prerequisites

- **Java 21** (JDK)
- **Maven 3.9+** (or use `./mvnw` wrapper)
- **Docker & Docker Compose**

---

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/ahMEDhat-7/Order-Inventory.git
cd Order-Inventory
```

### 2. Start infrastructure (PostgreSQL + Redis)

```bash
docker compose up -d postgres redis
```

### 3. Run the application

```bash
./mvnw spring-boot:run
```

### 4. Access Swagger UI

Open in browser: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### 5. Run with Docker Compose (full stack)

```bash
docker compose up --build
```

---

## Configuration

Key properties in `src/main/resources/application.yml`:

| Property | Default | Description |
|:---|:---|:---|
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/inventory_db` | PostgreSQL connection URL |
| `spring.datasource.username` | `postgres` | Database username |
| `spring.datasource.password` | `postgres` | Database password |
| `spring.jpa.hibernate.ddl-auto` | `validate` | Schema validation against Flyway |
| `spring.flyway.enabled` | `true` | Enable Flyway migrations |
| `spring.data.redis.host` | `localhost` | Redis host |
| `spring.data.redis.port` | `6379` | Redis port |
| `jwt.secret` | (base64-encoded) | HMAC-SHA key for JWT signing |
| `jwt.expiration-ms` | `86400000` | JWT token expiry (24 hours) |
| `server.port` | `8080` | Application server port |

---

## API Endpoints

### Authentication (Public)

| Method | Endpoint | Description |
|:---|:---|:---|
| `POST` | `/api/v1/auth/register` | Register a new user |
| `POST` | `/api/v1/auth/login` | Authenticate and receive JWT |

**Register:**
```json
POST /api/v1/auth/register
{
  "email": "user@example.com",
  "password": "SecurePassword123!"
}
Response: 201 Created
```

**Login:**
```json
POST /api/v1/auth/login
{
  "email": "user@example.com",
  "password": "SecurePassword123!"
}
Response: 200 OK
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer"
}
```

### Products (Requires JWT)

| Method | Endpoint | Access | Description |
|:---|:---|:---|:---|
| `GET` | `/api/v1/products` | `USER`, `ADMIN` | List all products (cached) |
| `GET` | `/api/v1/products/{id}` | `USER`, `ADMIN` | Get product by ID (cached) |
| `POST` | `/api/v1/products` | `ADMIN` | Create a new product |
| `PUT` | `/api/v1/products/{id}` | `ADMIN` | Update a product |
| `DELETE` | `/api/v1/products/{id}` | `ADMIN` | Delete a product |

**Create Product:**
```json
POST /api/v1/products
Authorization: Bearer <token>
{
  "sku": "PROD-1002",
  "name": "Ergonomic Mouse",
  "description": "Logitech MX Master 3S",
  "price": 99.99,
  "stockQuantity": 100
}
Response: 201 Created
```

### Orders (Requires JWT)

| Method | Endpoint | Access | Description |
|:---|:---|:---|:---|
| `POST` | `/api/v1/orders` | `USER`, `ADMIN` | Create an order (atomic stock deduction) |
| `GET` | `/api/v1/orders` | `USER`, `ADMIN` | Get order history for current user |

**Create Order:**
```json
POST /api/v1/orders
Authorization: Bearer <token>
{
  "items": [
    { "productId": 1, "quantity": 2 }
  ]
}
Response: 201 Created
{
  "orderId": 84,
  "status": "COMPLETED",
  "totalAmount": 259.98,
  "createdAt": "2026-09-17T01:17:00Z",
  "items": [
    {
      "productId": 1,
      "productName": "Wireless Mechanical Keyboard",
      "quantity": 2,
      "unitPrice": 129.99
    }
  ]
}
```

---

## Database Schema

4 tables managed by Flyway migrations:

```
users           products            orders              order_items
┌──────────┐   ┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│ id       │   │ id           │   │ id           │   │ id           │
│ email    │   │ sku          │   │ user_id (FK) │   │ order_id(FK) │
│ pass_hash│   │ name         │   │ status       │   │ product_id   │
│ role     │   │ description  │   │ total_amount │   │ quantity     │
│ created  │   │ price        │   │ created_at   │   │ unit_price   │
│          │   │ stock_qty    │   └──────────────┘   └──────────────┘
│          │   │ version      │
│          │   │ created_at   │
└──────────┘   └──────────────┘
```

| Table | Records | Key Features |
|:---|:---|:---|
| `users` | User accounts | BCrypt password hash, role column |
| `products` | Product catalog | Optimistic locking via `version` column |
| `orders` | Customer orders | Linked to user, status tracking |
| `order_items` | Order line items | Cascading delete from orders |

---

## Security

### Authentication Flow

```
Client → POST /auth/login → AuthService
  ↓ validates credentials (BCrypt)
  ↓ generates JWT (HMAC-SHA, 24h expiry)
  ↓ returns { token, type: "Bearer" }

Client → GET /api/v1/products (Authorization: Bearer <token>)
  ↓ JwtAuthenticationFilter intercepts request
  ↓ JwtUtil validates token + extracts email
  ↓ Loads User from UserRepository
  ↓ Sets SecurityContext with authorities
  ↓ Controller processes request
```

### Role-Based Access Control

| Endpoint | `ROLE_USER` | `ROLE_ADMIN` |
|:---|:---|:---|
| `POST /auth/register` | Public | Public |
| `POST /auth/login` | Public | Public |
| `GET /products` | Allowed | Allowed |
| `POST /products` | Denied | Allowed |
| `PUT /products/{id}` | Denied | Allowed |
| `DELETE /products/{id}` | Denied | Allowed |
| `POST /orders` | Allowed | Allowed |
| `GET /orders` | Allowed | Allowed |

---

## Dependency Injection

The project uses **constructor-based injection** exclusively via Lombok's `@RequiredArgsConstructor`. No `@Autowired` is used anywhere.

### How it works

```java
@Service
@RequiredArgsConstructor  // Lombok generates constructor for all final fields
public class OrderService {

    private final OrderRepository orderRepository;   // injected
    private final ProductRepository productRepository; // injected
    private final UserRepository userRepository;       // injected
    private final RedisTemplate<String, Object> redisTemplate; // injected
}
```

### Bean Dependency Map

| Bean | Injected Dependencies |
|:---|:---|
| `JwtUtil` | `@Value("${jwt.secret}")`, `@Value("${jwt.expiration-ms}")` |
| `JwtAuthenticationFilter` | `JwtUtil`, `UserRepository` |
| `SecurityConfig` | `JwtAuthenticationFilter` |
| `RedisConfig` | `RedisConnectionFactory` (auto-configured) |
| `AuthService` | `UserRepository`, `PasswordEncoder`, `JwtUtil` |
| `ProductService` | `ProductRepository`, `RedisTemplate` |
| `OrderService` | `OrderRepository`, `ProductRepository`, `UserRepository`, `RedisTemplate` |
| `AuthController` | `AuthService` |
| `ProductController` | `ProductService` |
| `OrderController` | `OrderService` |

**Annotations used:**
- `@Service` — Service layer beans
- `@Component` — Infrastructure beans (JwtUtil, JwtAuthenticationFilter)
- `@RestController` — Controller layer beans
- `@Configuration` + `@Bean` — Factory methods (SecurityConfig, RedisConfig)

---

## Caching Strategy

Redis is used for high-speed product catalog reads.

| Aspect | Detail |
|:---|:---|
| **Key Pattern** | `product:{id}` (e.g., `product:1`) |
| **TTL** | 30 minutes |
| **Serialization** | `GenericJackson2JsonRedisSerializer` |
| **Eviction** | On product update, delete, or stock change |

**Flow:**
```
GET /products/1
  → Check Redis for "product:1"
  → Cache HIT → return cached ProductResponse
  → Cache MISS → query PostgreSQL → cache result → return

PUT /products/1
  → Update PostgreSQL
  → Delete "product:1" from Redis (eviction)

POST /orders (deducts stock)
  → Update PostgreSQL stock
  → Delete "product:{id}" from Redis for each item
```

---

## Testing

### Unit Tests (Mockito)

| Test Class | Tests | What it covers |
|:---|:---|:---|
| `AuthServiceTest` | 4 | Register, login, duplicate email, invalid credentials |
| `ProductServiceTest` | 7 | CRUD, cache hit/miss, cache eviction, duplicate SKU |
| `OrderServiceTest` | 3 | Order creation, stock deduction, insufficient stock |

**Run unit tests:**
```bash
./mvnw test -Dtest="AuthServiceTest,ProductServiceTest,OrderServiceTest"
```

### Integration Tests (Testcontainers)

`AbstractIntegrationTest` spins up real Docker containers:

| Container | Image | Purpose |
|:---|:---|:---|
| PostgreSQL | `postgres:18-alpine` | Real database for JPA + Flyway |
| Redis | `redis:8-alpine` | Real cache for RedisTemplate |

```java
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:8-alpine");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.data.redis.host", redis::getHost);
        // ... injects container connection details
    }
}
```

**Run all tests (requires Docker):**
```bash
./mvnw test
```

---

## Docker

### Multi-Stage Dockerfile

```dockerfile
# Stage 1: Build
FROM eclipse-temurin:21-jdk AS build
COPY . .
RUN ./mvnw package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Docker Compose Services

| Service | Image | Port | Description |
|:---|:---|:---|:---|
| `postgres` | `postgres:18-alpine` | 5432 | PostgreSQL database |
| `redis` | `redis:8-alpine` | 6379 | Redis cache |
| `app` | Built from `Dockerfile` | 8080 | Spring Boot API |

```bash
# Start full stack
docker compose up --build

# Start only infrastructure
docker compose up -d postgres redis

# Stop all
docker compose down
```

---

## CI/CD

GitHub Actions pipeline on push/PR to `main`:

```yaml
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - Checkout code
      - Set up JDK 21 (Temurin)
      - Build with Maven (compile)
      - Run all tests (Testcontainers spins up Docker containers)
      - Package JAR
```

**Pipeline stages:**
1. `./mvnw compile -B` — Compile source code
2. `./mvnw test -B` — Run unit + integration tests
3. `./mvnw package -DskipTests -B` — Build executable JAR

---

## API Documentation

Once the application is running:

| Resource | URL |
|:---|:---|
| **Swagger UI** | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) |
| **OpenAPI JSON** | [http://localhost:8080/api-docs](http://localhost:8080/api-docs) |

---

## License

This project is licensed under the MIT License.
