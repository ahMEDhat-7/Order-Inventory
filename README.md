# Order & Inventory Processing Service

A high-performance, enterprise-grade backend service built with **Java 21, Spring Boot 4, Spring Data JPA, PostgreSQL, and Redis**. Designed with layered architecture, robust security, and optimistic locking for reliable inventory management.

## Key Features

- **Security:** Stateless JWT Authentication with Spring Security and Role-Based Access Control (RBAC).
- **Transactional Consistency:** Atomic order placement handling stock reduction with optimistic locking (`@Version`) to prevent race conditions.
- **Caching Layer:** Redis cache integration for high-speed product catalog reads and automatic cache eviction on updates.
- **Comprehensive Testing:** Unit testing using JUnit 5 & Mockito, plus integration testing with Testcontainers.
- **API Documentation:** Interactive Swagger UI documentation powered by OpenAPI 3.

## Tech Stack

- **Java 21** | **Spring Boot 4** | **Spring Security**
- **Spring Data JPA** | **Hibernate** | **PostgreSQL**
- **Redis** | **JUnit 5** | **Docker & Docker Compose**
