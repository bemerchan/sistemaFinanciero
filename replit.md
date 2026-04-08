# Mini Sistema Financiero - Flypass

## Overview

Prueba técnica Flypass: aplicación web para gestión de clientes y cuentas bancarias.
El proyecto está organizado como un monorepo pnpm con backend Java/Spring Boot y frontend Angular 18.

## Stack

### Backend (Java / Spring Boot)
- **Framework**: Spring Boot 3.2.5
- **Java version**: 17 (GraalVM 22.3)
- **Database**: PostgreSQL + Spring Data JPA (Hibernate)
- **SDR**: Spring Data REST en `/api/v1` + custom controllers
- **Validation**: Bean Validation (jakarta.validation)
- **Documentation**: SpringDoc OpenAPI 2.x (Swagger UI)
- **Utilities**: Lombok
- **Build**: Maven 3.8.6
- **Port**: 9000

### Frontend (Angular 18)
- **Framework**: Angular 18 (standalone components, lazy-loaded routes)
- **UI Library**: Angular Material 18 (tema azure-blue, M3)
- **HTTP**: Angular HttpClient con proxy hacia backend (puerto 9000)
- **Forms**: ReactiveFormsModule (FormGroup, FormBuilder, Validators)
- **Routing**: Angular Router con carga lazy de features
- **Build tool**: Angular CLI (esbuild builder)
- **Preview path**: `/` (port 24069)

### Monorepo tooling (Node.js)
- **Monorepo tool**: pnpm workspaces
- **Node.js version**: 24
- **Package manager**: pnpm

## Project Structure

```
/
├── backend/                          # Spring Boot API (puerto 9000)
│   ├── pom.xml
│   └── src/main/java/com/flypass/financial/
│       ├── config/                   # CORS, JPA, Swagger config
│       ├── controller/               # REST controllers custom
│       ├── dto/                      # Request/Response DTOs
│       ├── entity/                   # JPA entities
│       ├── exception/                # ApiException + GlobalExceptionHandler
│       ├── repository/               # Spring Data JPA + SDR repos
│       └── service/impl/
│
└── artifacts/
    └── financial-frontend/           # Angular 18 app (puerto 24069)
        ├── angular.json
        ├── package.json
        ├── proxy.conf.json           # Proxy /api/v1 → localhost:9000
        └── src/
            └── app/
                ├── core/
                │   ├── models/       # Customer, Account, Transaction models
                │   └── services/     # CustomerService, AccountService, TransactionService
                ├── shared/
                │   └── components/
                │       ├── navbar/           # Barra de navegación
                │       └── confirm-dialog/   # Diálogo de confirmación reutilizable
                └── features/
                    ├── customers/            # Lista de clientes + formulario CRUD
                    │   └── customer-form-dialog/
                    └── customer-detail/      # Detalle cliente + cuentas
                        ├── account-card/     # Tarjeta expansible por cuenta
                        ├── transaction-form/ # Formulario de transacción
                        └── transaction-list/ # Últimas 5 transacciones
```

## API Endpoints

### SDR (Spring Data REST) — HAL+JSON
- `GET /api/v1/customers?size=200&sort=id,desc` — lista clientes
- `GET /api/v1/customers/{id}` — cliente por ID
- `GET /api/v1/accounts/search/byCustomer?customerId={id}` — cuentas de cliente
- `GET /api/v1/transactions/search/byAccount?accountId={id}&page=0&size=5` — últimas transacciones

### Custom Controllers — `ApiResponse<T>` JSON
- `POST /api/v1/customers` — crear cliente
- `PUT /api/v1/customers/{id}` — actualizar cliente
- `DELETE /api/v1/customers/{id}` — eliminar cliente (sin cuentas)
- `POST /api/v1/accounts` — crear cuenta (SAVINGS/CHECKING)
- `DELETE /api/v1/accounts/{id}` — eliminar cuenta
- `POST /api/v1/transactions/account/{id}` — registrar transacción

## Business Rules
- Age ≥ 18 años
- Email e identificación únicos por cliente
- Números de cuenta: 53XXXXXXXX (ahorro) / 33XXXXXXXX (corriente)
- Saldo mínimo $0 en ahorro
- No se puede eliminar cliente con cuentas

## Key Commands

### Backend
- `cd backend && mvn clean package -DskipTests` — build JAR
- `java -jar backend/target/financial-system-0.0.1-SNAPSHOT.jar --server.port=9000` — ejecutar

### Frontend
- `pnpm --filter @workspace/financial-frontend run dev` — dev server

## Swagger UI
Disponible en: http://localhost:9000/swagger-ui.html
