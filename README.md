<br />
<div align="center">
<h3 align="center">Order Service</h3>

<p align="center">
A high-performance Spring Boot microservice for order processing, featuring cross-service communication, resilient architecture, and automated delivery pipelines.
<br />
</div>

<details>
<summary>Table of Contents</summary>
<ol>
<li>
<a href="#about-the-project">About The Project</a>
<ul>
<li><a href="#built-with">Built With</a></li>
</ul>
</li>
<li><a href="#key-features">Key Features</a></li>
<li>
<a href="#getting-started">Getting Started</a>
<ul>
<li><a href="#prerequisites">Prerequisites</a></li>
<li><a href="#installation">Installation</a></li>
</ul>
</li>
<li><a href="#api-endpoints">API Endpoints</a></li>
<li><a href="#database-architecture">Database Architecture</a></li>
<li><a href="#technical-implementation">Technical Implementation</a></li>
<li><a href="#testing--quality">Testing & Quality</a></li>
<li><a href="#ci-cd-pipeline">CI/CD Pipeline</a></li>
<li><a href="#environment-variables">Environment Variables</a></li>
</ol>
</details>

## About The Project

Order Service is a core microservice responsible for the lifecycle of customer orders. It manages the relationship between users and products (items), handles complex status transitions, and ensures data integrity through transactional operations and soft-delete mechanisms.

### Built With

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-green?style=for-the-badge&logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?style=for-the-badge&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-✓-blue?style=for-the-badge&logo=docker)
![Liquibase](https://img.shields.io/badge/Liquibase-✓-blue?style=for-the-badge&logo=liquibase)

---

## Key Features

* **Full Order Lifecycle**: CRUD operations with automated total price calculation.
* **Cross-Service Communication**: Integration with **User Service** via OpenFeign to enrich order data with user details.
* **Fault Tolerance**: Circuit Breaker pattern implementation to handle User Service outages.
* **Advanced Filtering**: Search orders by status and creation date ranges using JPA Specifications.
* **Soft Delete**: Data preservation strategy using logical deletion.
* **Automated Schema**: Database versioning and migrations with Liquibase.
* **Validation**: Strict DTO validation to ensure data consistency.

---

## API Endpoints

All responses (except DELETE) are wrapped in `ResponseEntity` and contain enriched user information.

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/v1/orders` | Create a new order |
| `GET` | `/api/v1/orders/{id}` | Get order by ID (with user info) |
| `GET` | `/api/v1/orders/user/{userId}` | Get all orders for a specific user |
| `GET` | `/api/v1/orders` | Search orders (pagination, status, date range) |
| `PATCH` | `/api/v1/orders/{id}` | Update order details/status |
| `DELETE` | `/api/v1/orders/{id}` | Soft delete an order |

---

## Database Architecture

### Schema Overview

**Tables**:

* **`orders`**: Core order data (total price, status, user reference).
* **`order_items`**: Junction table for Many-to-Many relationship with quantity tracking.
* **`items`**: Catalog of available products.

### Auditing & Optimization

* **JPA Auditing**: `CreatedAt` and `UpdatedAt` timestamps are automatically managed.
* **Indexing**: Optimized lookups for `user_id` and order `status`.
* **Soft Delete**: Filtered by a `deleted` flag on all queries.

---

## Technical Implementation

### Data Mapping

The service uses **MapStruct** for high-performance mapping between DAO Entities and REST DTOs, keeping the layers strictly decoupled.

### Resilience

The **Circuit Breaker** pattern protects the service from cascading failures when fetching user information from the external User Service.

### Transaction Management

Strict use of `@Transactional` for all state-changing operations (`UPDATE`, `DELETE`, `CREATE`) ensures atomicity and data consistency, especially when handling `CascadeType` operations.

---

## Testing & Quality

The project maintains high stability through multiple testing layers:

* **Unit Testing**: 100% coverage of business logic in Service Layer (JUnit 5 & Mockito).
* **Integration Testing**:
* **Testcontainers**: Real PostgreSQL instances for database verification.
* **WireMock**: Mocking external User Service API responses and testing Circuit Breaker scenarios.



```bash
# Run tests
./mvnw clean test

# Integration tests only
./mvnw test -Dtest="*ControllerTest"

# Unit tests only
./mvnw test -Dtest="*ServiceImplTest"

```
### API Documentation
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI Spec: `http://localhost:8080/v3/api-docs`

---

## CI/CD Pipeline

The project uses **GitHub Actions** for continuous integration and delivery:

1. **Build**: Compiles code and verifies dependencies.
2. **Testing**: Runs unit and integration tests (using Docker-in-Docker for Testcontainers).
3. **Code Analysis**: SonarQube scan for technical debt.
4. **Artefact Creation**: Builds a Docker image and pushes it to the registry.

---

## Environment Variables

| Variable | Description | Default                    |
| --- | --- |----------------------------|
| `DB_HOST` | PostgreSQL host | `localhost`                |
| `DB_PORT` | PostgreSQL port | `5432`                     |
| `DB_NAME` | Database name | `order_db`                 |
| `DB_USERNAME` | DB user | `postgres`                 |
| `DB_PASSWORD` | DB password | `password`                 |
| `USER_SERVICE_URL` | URL for User Service integration | `http://user-service:8081` |

---