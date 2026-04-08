# Mini Sistema Financiero - Flypass

## Overview

Prueba técnica Flypass: aplicación web para gestión de clientes y cuentas bancarias.
El proyecto está organizado como un monorepo con backend Java/Spring Boot y frontend Angular (próximamente).

## Stack

### Backend (Java / Spring Boot)
- **Framework**: Spring Boot 3.2.5
- **Java version**: 17 (GraalVM 22.3)
- **Database**: PostgreSQL + Spring Data JPA (Hibernate)
- **Validation**: Bean Validation (jakarta.validation)
- **Documentation**: SpringDoc OpenAPI 2.x (Swagger UI)
- **Utilities**: Lombok
- **Build**: Maven 3.8.6

### Frontend (React + Vite)
- **Framework**: React 18 + Vite
- **UI**: shadcn/ui (Radix UI) + Tailwind CSS
- **State/data**: React Query (@tanstack/react-query)
- **Forms**: react-hook-form + zod
- **Routing**: wouter
- **Preview path**: `/` (port assigned via PORT env var)

### Monorepo tooling (Node.js)
- **Monorepo tool**: pnpm workspaces
- **Node.js version**: 24
- **Package manager**: pnpm
- **TypeScript version**: 5.9

## Project Structure

```
/
├── backend/                    # Spring Boot API
│   ├── pom.xml
│   └── src/main/java/com/flypass/financial/
│       ├── config/             # CORS, JPA, Swagger config
│       ├── controller/         # REST controllers
│       ├── dto/
│       │   ├── request/        # Input DTOs
│       │   └── response/       # Output DTOs
│       ├── entity/             # JPA entities
│       ├── exception/          # Custom exceptions + GlobalExceptionHandler
│       ├── repository/         # Spring Data JPA repositories
│       └── service/
│           └── impl/           # Service implementations
├── README.md                   # Project documentation
└── artifacts/                  # Node.js artifacts (legacy)
```

## Key Commands

### Backend
- `cd backend && mvn clean compile` — compile
- `cd backend && mvn spring-boot:run` — run server (requires PostgreSQL)
- `cd backend && mvn test` — run tests

### Backend Environment Variables
- `DB_HOST` (default: localhost)
- `DB_PORT` (default: 5432)
- `DB_NAME` (default: financial_db)
- `DB_USERNAME` (default: postgres)
- `DB_PASSWORD` (default: postgres)
- `SERVER_PORT` (default: 8080)

## API Endpoints

### Customers: `/api/v1/customers`
- POST, GET, GET/{id}, PUT/{id}, DELETE/{id}

### Accounts: `/api/v1/accounts`
- POST, GET/customer/{customerId}, GET/{id}, GET/{id}/balance

### Transactions: `/api/v1/transactions`
- POST/account/{accountId}, GET/account/{accountId}, GET/account/{accountId}/last

## Swagger UI
Available at: http://localhost:8080/swagger-ui.html
