# Human Resource Management Backend

This document describes our codebase, including:

- Source code structure and main backend modules.
- Coding guideline and convention for adding a new API.
- How to run unit, API, security, stress, and integration tests.
- How to set up and run the backend locally.
- API documentation with Swagger/OpenAPI.
- API testing collection guidance for Postman so the APIs can be tested against the local setup.

## 1. Project Overview

This is the backend service for a Human Resource Management (HRM) system. It is built with Java 21, Spring Boot, PostgreSQL, Spring Security, JWT, Flyway, and Swagger/OpenAPI.

The backend handles:

- Employee, department, and contract management.
- Attendance tracking with shift, holiday, GPS, and IP validation.
- Leave type, leave balance, and leave request workflows.
- Overtime request approval.
- Payroll generation with attendance, overtime, allowances, deductions, insurance, and personal income tax.
- Role-based access control for `ADMIN` and `EMPLOYEE`.

## 2. Tech Stack

| Area | Technology |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 4.0.5 |
| Persistence | Spring Data JPA, Hibernate |
| Database | PostgreSQL 16 |
| Migration | Flyway |
| Security | Spring Security, JWT |
| API Documentation | Springdoc OpenAPI, Swagger UI |
| Testing | JUnit 5, Mockito, MockMvc, H2 |
| Containerization | Docker, Docker Compose |

## 3. Source Code

Main source code is located under:

```text
src/main/java/com/hrm/backend
```

Package structure:

```text
src/main/java/com/hrm/backend
+-- config          # Application, CORS, async, and security configuration
+-- controller      # REST API controllers
+-- dto             # Request and response DTOs
+-- entity          # JPA entities mapped to database tables
+-- exception       # Global exception handling
+-- repository      # Spring Data JPA repositories
+-- scheduler       # Scheduled background jobs
+-- security        # JWT filter, token provider, user details service
+-- service         # Business service interfaces and implementations
```

Database migrations are located under:

```text
src/main/resources/db/migration
+-- V1__init_schema.sql
+-- V2__functions_and_procedures.sql
+-- V3__seed_master_data.sql
+-- V4__seed_mock_data.sql
```

Test source code is located under:

```text
src/test/java/com/hrm/backend
```

Generated test reports are located under:

```text
docs/test-reports/
```

## 4. Main Modules

| Module | Responsibility |
| --- | --- |
| Auth | Login, current user, password change, forgot password flow |
| Employee | Employee profile, status, resignation, department assignment |
| Department | Department CRUD, manager assignment, parent-child hierarchy |
| Contract | Contract lifecycle, activation, termination, compliance rules |
| Attendance | Check-in, check-out, daily/monthly records, GPS/IP validation |
| Shift | Working shift configuration |
| Holiday | Public holiday configuration and payroll multiplier |
| Leave Type | Leave policy configuration |
| Leave Balance | Annual leave balance tracking |
| Leave Request | Leave request submission, approval, rejection, cancellation |
| Overtime Request | Overtime registration and approval |
| Payroll | Monthly payroll generation, approval, payment, deductions |

## 5. Coding Guideline & Convention

### Layering Rules

- Controllers only handle HTTP request/response mapping and validation.
- Business rules must be implemented in service classes.
- Repositories only handle database access.
- API payloads should use DTOs instead of exposing entities directly.
- Common error responses should go through `GlobalExceptionHandler`.
- Database schema changes must be added through Flyway migrations.

### Naming Convention

| Type | Convention | Example |
| --- | --- | --- |
| Controller | `<Domain>Controller` | `EmployeeController` |
| Service interface | `<Domain>Service` | `PayrollService` |
| Service implementation | `<Domain>ServiceImpl` | `PayrollServiceImpl` |
| Repository | `<Domain>Repository` | `EmployeeRepository` |
| Request DTO | `<Action/Domain>Request` | `EmployeeRequest` |
| Response DTO | `<Domain>Response` | `PayrollResponse` |
| Entity | Singular noun | `Employee`, `Payroll` |

### How To Code A New API

1. Add or update the database schema in `src/main/resources/db/migration` if the API requires schema changes.
2. Create or update the JPA entity in `entity`.
3. Create request/response DTOs in `dto`.
4. Add repository methods in `repository`.
5. Define business behavior in a service interface under `service`.
6. Implement the behavior in `service/impl`.
7. Expose the endpoint in a controller under `controller`.
8. Add validation annotations to request DTOs where needed.
9. Add unit tests for business rules.
10. Add MockMvc tests for API behavior.
11. Verify the endpoint in Swagger or Postman against the local setup.

### API Response Convention

APIs should return the common response wrapper:

```java
ApiResponse<T>
```

Use consistent HTTP status codes:

| Status | Usage |
| --- | --- |
| `200 OK` | Successful read/update/action |
| `201 Created` | Successful create |
| `400 Bad Request` | Invalid input or business rule violation |
| `401 Unauthorized` | Missing or invalid authentication |
| `403 Forbidden` | Authenticated but not allowed |
| `404 Not Found` | Resource does not exist |
| `500 Internal Server Error` | Unexpected system error |

## 6. Local Setup

### Prerequisites

- Java 21
- Maven 3.9+
- PostgreSQL 16
- Docker and Docker Compose, optional but recommended

### Environment Variables

The backend reads configuration from `src/main/resources/application.yml` and environment variables.

| Variable | Default | Description |
| --- | --- | --- |
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `HRM` | Database name |
| `DB_USERNAME` | `postgres` | Database username |
| `DB_PASSWORD` | `123456` | Database password |
| `SMTP_USERNAME` | configured in `application.yml` | SMTP username |
| `SMTP_PASSWORD` | configured in `application.yml` | SMTP app password |

For real deployment, replace default credentials, SMTP settings, and JWT secret with environment-specific values.

### Run With Maven

Create a PostgreSQL database named `HRM`, then run:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

Backend URL:

```text
http://localhost:8080
```

### Run With Docker Compose

From the repository root:

```bash
docker compose up --build
```

Services:

| Service | URL |
| --- | --- |
| PostgreSQL | `localhost:5432` |
| Backend API | `http://localhost:8080` |
| Frontend | `http://localhost:5173` |

## 7. How To Run Tests

Run all backend tests:

```bash
./mvnw test
```

On Windows:

```bash
mvnw.cmd test
```

Current test coverage includes:

- Unit tests for payroll, tax, insurance, attendance, contracts, leave, overtime, and department hierarchy.
- MockMvc API tests for authentication and HRM endpoints.
- Security tests for JWT tampering, expired tokens, SQL injection, and XSS payloads.
- Stress tests for concurrent attendance scenarios.
- End-to-end integration tests for core backend workflows.

Test output and generated reports:

```text
docs/test-reports/TEST_RESULT.md
docs/test-reports/TEST_GUIDE.md
```

## 8. API Documents

Swagger UI is available after the backend starts:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON is available at:

```text
http://localhost:8080/v3/api-docs
```

All APIs are versioned under:

```text
/api/v1
```

Main API groups:

| Module | Base Path |
| --- | --- |
| Authentication | `/api/v1/auth` |
| Employees | `/api/v1/employees` |
| Departments | `/api/v1/departments` |
| Contracts | `/api/v1/contracts` |
| Attendance | `/api/v1/attendance` |
| Shifts | `/api/v1/shifts` |
| Holidays | `/api/v1/holidays` |
| Leave Types | `/api/v1/leave-types` |
| Leave Balances | `/api/v1/leave-balances` |
| Leave Requests | `/api/v1/leave-requests` |
| Overtime Requests | `/api/v1/overtime-requests` |
| Payrolls | `/api/v1/payrolls` |

## 9. API Testing Collection

Postman can be used to test the local backend.

Recommended local environment variables:

| Variable | Value |
| --- | --- |
| `baseUrl` | `http://localhost:8080/api/v1` |
| `accessToken` | Token returned from login API |

Login request:

```http
POST {{baseUrl}}/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

Authenticated requests should include:

```http
Authorization: Bearer {{accessToken}}
```

Recommended collection location if exported to the repository:

```text
docs/postman/hrm.postman_collection.json
docs/postman/hrm.local.postman_environment.json
```

The collection should target `{{baseUrl}}`, so it works without changes when the backend runs locally on port `8080`.

## 10. Project Highlights

- Flyway-based schema migration with Hibernate schema validation.
- Clear controller-service-repository separation.
- DTO-based API contracts.
- Centralized exception handling.
- JWT authentication and role-based authorization.
- Payroll logic for attendance-based salary, approved overtime, insurance, PIT, holiday handling, and leave balance adjustment.
- Automated backend tests covering unit, API, security, stress, and integration levels.

## Author

Tran Si Cuong
