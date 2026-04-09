# WorkScholr Backend

Spring Boot backend for the WorkScholr student work-study platform.

## Tech stack

- Spring Boot 3.3.5
- Spring Security
- Spring Data JPA
- MySQL
- JWT authentication
- Swagger / OpenAPI
- DTOs + ModelMapper

## Features

- Student registration and login
- Admin login
- JWT token generation and route protection
- Job posting and management
- Applications, approvals, and feedback
- Work-log submission and approval
- Global exception handling

## Database setup

Create the database if needed:

```sql
CREATE DATABASE workscholr_db;
```

## Required environment variables

- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`

## Optional environment variables

- `DB_URL`
- `SEED_DEMO_DATA=true`
- `BOOTSTRAP_ADMIN_ENABLED=true`
- `BOOTSTRAP_ADMIN_EMAIL`
- `BOOTSTRAP_ADMIN_PASSWORD`
- `BOOTSTRAP_ADMIN_FULL_NAME`

Public registration creates student accounts only. Admin accounts should be seeded or bootstrapped manually.

## Run

```bash
mvn spring-boot:run
```

Or run `WorkScholrBackendApplication` from Spring Tools Suite.

## Tests

```bash
mvn test
```

## Swagger

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Main API routes

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/jobs`
- `POST /api/jobs`
- `PUT /api/jobs/{id}`
- `DELETE /api/jobs/{id}`
- `POST /api/applications`
- `GET /api/applications/my`
- `GET /api/applications`
- `PATCH /api/applications/{id}/status`
- `POST /api/worklogs`
- `GET /api/worklogs/my`
- `GET /api/worklogs`
- `PATCH /api/worklogs/{id}/status`
- `POST /api/feedback`
- `GET /api/feedback/my`
- `GET /api/feedback/application/{applicationId}`
- `GET /api/admin/dashboard`
