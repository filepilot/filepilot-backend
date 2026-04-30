# FilePilot — Backend

REST API for the FilePilot document version control system. Built with Spring Boot.

## Tech Stack

- **Java 21**
- **Spring Boot 3.2.5** (Web, Data JPA, Security)
- **PostgreSQL 16**
- **Maven**

## What It Does

All business logic of the system lives here:
- User registration, login, JWT authentication
- Role-based access control (Author / Reviewer / Reader / Admin)
- Document CRUD
- Version management (create, submit, approve, reject)
- Diff between versions
- Export to PDF / TXT
- Audit log of every action

## Requirements

- Java 17
- Maven 3.9+
- PostgreSQL 16 running on `localhost:5432`

## Setup

1. Create the database:
   ```sql
   CREATE DATABASE filepilot_db;
   CREATE USER filepilot WITH PASSWORD 'filepilot';
   GRANT ALL PRIVILEGES ON DATABASE filepilot_db TO filepilot;
   ```

2. Configure `src/main/resources/application.yml` (or use the provided `.env`):
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/filepilot_db
       username: filepilot
       password: filepilot
   ```

3. Run the app:
   ```bash
   mvn spring-boot:run
   ```

The API starts on **http://localhost:8080**.

## API Documentation

Swagger UI is available at: **http://localhost:8080/swagger-ui.html**

## Main Endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/auth/register` | Register a new user |
| `POST` | `/api/auth/login` | Login, get JWT token |
| `GET`  | `/api/documents` | List documents |
| `POST` | `/api/documents` | Create document |
| `POST` | `/api/documents/{id}/versions` | Create new version |
| `PUT`  | `/api/versions/{id}/approve` | Approve version |
| `GET`  | `/api/versions/{id1}/diff/{id2}` | Compare two versions |

Full list in Swagger.

## Run Tests

```bash
mvn test
```

## Project Structure

```
src/main/java/com/filepilot/vcs/
├── controller/   REST endpoints
├── service/      Business logic
├── repository/   Database access (Spring Data JPA)
├── model/        JPA entities
├── dto/          Request / response objects
├── exception/    Global error handling
└── config/       Spring Security, CORS, Swagger
```

## Build for Production

```bash
mvn clean package
java -jar target/vcs-0.0.1-SNAPSHOT.jar
```
