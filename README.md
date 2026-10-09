# SyncSpace — Team Collaboration & Project Management

SyncSpace is a portfolio-ready full-stack application for organizing workspaces, projects, Kanban tasks, comments, and team activity. It uses a React single-page app, a Spring Boot REST API, PostgreSQL, JPA/Hibernate, and JWT authentication.

## Features

- Secure registration and login with BCrypt password hashing and JWT tokens
- Personal dashboard with workspace and project summaries
- Workspaces with owners and member management
- Projects inside workspaces
- Kanban-style tasks: To do, In progress, and Done
- Task priorities, due dates, assignees, comments, activity history, and notifications
- Server-side validation, authorization checks, and consistent API error responses
- Demo seed data for a quick first look

## Stack

| Area | Technology |
| --- | --- |
| Frontend | React 18, Vite, React Router, plain CSS |
| Backend | Java 25, Spring Boot 4.1, Spring Security, Spring Data JPA |
| Database | H2 (local default) or PostgreSQL 15+ |
| Security | JWT (`jjwt`) and BCrypt |

## Run locally

### Prerequisites

- Java 25+
- Maven 3.9+
- Node.js 18+
- PostgreSQL 15+ (optional; the backend defaults to an in-memory H2 database)

### 1. Optional: Create a PostgreSQL database

The backend runs locally without PostgreSQL by using an in-memory H2 database. To use PostgreSQL instead, create an empty `syncspace` database. Hibernate will create/update tables automatically in development.

```sql
CREATE DATABASE syncspace;
```

### 2. Run the backend

From `backend`, run the app. It uses the in-memory H2 database by default; its data is reset when the process stops.

```powershell
mvn -s maven-settings.xml spring-boot:run
```

To use PostgreSQL, set its connection details in the same PowerShell session before starting Maven:

```powershell
$env:DB_URL='jdbc:postgresql://localhost:5432/syncspace'
$env:DB_USERNAME='postgres'
$env:DB_PASSWORD='<your-local-postgres-password>'
mvn -s maven-settings.xml spring-boot:run
```

The API starts at `http://localhost:8080/api`. The included Maven settings file keeps Maven's downloaded cache inside the project directory; it can be omitted on a standard developer machine.

### 3. Run the frontend

In another terminal:

```powershell
cd frontend
npm install
npm run dev
```

Open the address Vite shows (normally `http://localhost:5173`). Register an account, or sign in using the seeded account:

`demo@syncspace.dev` / `Password123!`

## API overview

All protected endpoints require `Authorization: Bearer <token>`.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| POST | `/api/auth/register` | Create an account |
| POST | `/api/auth/login` | Get a JWT |
| GET | `/api/users/me` | Current user |
| GET/POST | `/api/workspaces` | List/create workspaces |
| GET/PUT/DELETE | `/api/workspaces/{id}` | Workspace details/manage |
| POST | `/api/workspaces/{id}/members` | Add a member by email |
| GET/POST | `/api/workspaces/{id}/projects` | List/create projects |
| GET/PUT/DELETE | `/api/projects/{id}` | Project details/manage |
| GET/POST | `/api/projects/{id}/tasks` | List/create tasks |
| PUT/DELETE | `/api/tasks/{id}` | Update/delete task |
| GET/POST | `/api/tasks/{id}/comments` | List/add task comments |
| GET | `/api/notifications` | Current user notifications |

Example login:

```json
POST /api/auth/login
{ "email": "demo@syncspace.dev", "password": "Password123!" }
```

## Project structure

```text
backend/                 Spring Boot REST API
  src/main/java/...      entities, repositories, services, controllers, security
  src/main/resources/    application configuration and SQL reference schema
frontend/                React + Vite single-page application
  src/                   pages, components, API client, styles
```

## Deliberately planned enhancements

The following are intentionally not included so the core project remains simple to run locally:

- WebSocket live updates and chat
- Redis caching/presence
- Docker Compose deployment
- Swagger/OpenAPI UI
- Expanded unit/integration/e2e tests
- File attachments, invitation emails, and fine-grained roles

These are good next portfolio iterations; the REST API has clear seams for each.

## Notes for production

Change `JWT_SECRET`, disable `ddl-auto: update`, use migrations (Flyway/Liquibase), restrict CORS, move secrets to a secret manager, and serve behind HTTPS.
