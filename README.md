# Mini Sistema Financiero — Flypass

Sistema web full-stack para gestión de clientes, cuentas bancarias y transacciones, desarrollado como prueba técnica para Flypass.

---

## Tabla de contenidos

1. [Stack tecnológico](#stack-tecnológico)
2. [Arquitectura del proyecto](#arquitectura-del-proyecto)
3. [Reglas de negocio](#reglas-de-negocio)
4. [API REST — Endpoints](#api-rest--endpoints)
5. [Manejo de errores](#manejo-de-errores)
6. [Cómo correr localmente](#cómo-correr-localmente)
7. [Tests y cobertura](#tests-y-cobertura)
8. [Base de datos](#base-de-datos)
9. [Variables de entorno](#variables-de-entorno)
10. [Despliegue en Render](#despliegue-en-render)

---

## Stack tecnológico

### Backend — `backend/`

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 17 | Lenguaje principal |
| Spring Boot | 3.2.5 | Framework principal |
| Spring Data REST | incluido | Exposición de repositorios como API REST |
| Spring Data JPA + Hibernate | incluido | ORM y persistencia |
| PostgreSQL | 14+ | Base de datos en producción |
| SpringDoc OpenAPI | 2.5.0 | Documentación Swagger automática |
| Lombok | incluido | Reducción de código repetitivo |
| Maven | 3.8+ | Gestión de dependencias y build |
| H2 (test) | incluido | BD en memoria para tests |
| JaCoCo | 0.8.11 | Cobertura de código |
| JUnit 5 + Mockito | incluido | Framework de testing |

### Frontend — `artifacts/financial-frontend/`

| Tecnología | Versión | Uso |
|---|---|---|
| Angular | 18 | Framework principal |
| Angular Material | 18 | Componentes UI (tema azure-blue, M3) |
| TypeScript | 5.x | Lenguaje principal |
| RxJS | 7.x | Programación reactiva |
| Angular Router | 18 | Navegación con lazy loading |
| ReactiveFormsModule | 18 | Formularios reactivos con validación |
| pnpm | 9+ | Gestor de paquetes (workspace monorepo) |

---

## Arquitectura del proyecto

```
/
├── backend/                              # Spring Boot API
│   ├── Dockerfile                        # Build Docker multi-etapa (Maven + JRE Alpine)
│   ├── render.yaml                       # Despliegue independiente en Render
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/flypass/financial/
│       │   │   ├── config/               # CORS, JPA Auditing, Swagger, Web
│       │   │   ├── controller/           # CustomerController, AccountController, TransactionController
│       │   │   ├── dto/
│       │   │   │   ├── request/          # CustomerRequest, AccountRequest, TransactionRequest
│       │   │   │   └── response/         # CustomerResponse, AccountResponse, TransactionResponse
│       │   │   ├── entity/               # Customer, Account, Transaction (JPA)
│       │   │   ├── exception/            # ApiException, GlobalExceptionHandler
│       │   │   ├── handler/              # CustomerEventHandler (eventos Spring Data REST)
│       │   │   ├── model/                # ApiResponse, ErrorResponse
│       │   │   ├── repository/           # CustomerRepository, AccountRepository, TransactionRepository
│       │   │   └── service/
│       │   │       └── impl/             # CustomerServiceImpl, AccountServiceImpl, TransactionServiceImpl
│       │   └── resources/
│       │       ├── application.yml       # Configuración principal
│       │       └── db/schema.sql         # DDL de PostgreSQL
│       └── test/
│           ├── java/com/flypass/financial/
│           │   ├── integration/          # Tests con Spring context + H2
│           │   └── unit/                 # Tests unitarios (entidades, DTOs, excepciones, servicios)
│           └── resources/
│               ├── application-test.yml  # H2 PostgreSQL-mode
│               └── schema-h2.sql         # Secuencias para H2
│
├── artifacts/financial-frontend/         # Angular SPA
│   ├── render.yaml                       # Despliegue independiente en Render
│   ├── src/
│   │   ├── app/
│   │   │   ├── core/
│   │   │   │   ├── models/               # Interfaces TypeScript (Customer, Account, Transaction)
│   │   │   │   └── services/             # CustomerService, AccountService, TransactionService, ApiService
│   │   │   ├── features/
│   │   │   │   ├── customers/            # Lista de clientes + acciones CRUD
│   │   │   │   └── customer-detail/      # Detalle: cuentas y transacciones por cliente
│   │   │   └── shared/                   # Componentes reutilizables
│   │   └── environments/
│   │       ├── environment.ts            # Config desarrollo (URL relativa, proxy)
│   │       └── environment.prod.ts       # Config producción (URL absoluta Render)
│   └── public/
│       └── _redirects                    # Fallback SPA (Angular Router)
│
├── database/
│   ├── 01_schema.sql                     # DDL: secuencias, tablas, índices
│   └── 02_seed.sql                       # Datos de prueba (clientes, cuentas, transacciones)
│
└── render.yaml                           # Despliegue combinado (backend + frontend + BD)
```

---

## Reglas de negocio

| Regla | Detalle |
|---|---|
| Edad mínima | No se permite registrar ni actualizar clientes menores de 18 años |
| Email único | No se pueden registrar dos clientes con el mismo email |
| Identificación única | No se pueden registrar dos clientes con el mismo número de identificación |
| Eliminación protegida | No se puede eliminar un cliente que tenga cuentas vinculadas |
| Número de cuenta | Autogenerado, 10 dígitos, único. Ahorro: `53XXXXXXXX` · Corriente: `33XXXXXXXX` |
| Estado inicial | Toda cuenta nueva se crea en estado `ACTIVE` |
| Saldo mínimo | Las cuentas de ahorro no pueden quedar con saldo negativo (`UNPROCESSABLE_ENTITY`) |
| Cuenta inactiva | No se pueden registrar transacciones en cuentas con estado `INACTIVE` |
| Tipos de identificación | `CC`, `CE`, `TI`, `PASSPORT`, `NIT` |

---

## API REST — Endpoints

Base path: `/api/v1`

### Clientes

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/customers` | Listar todos los clientes |
| `GET` | `/customers/{id}` | Obtener cliente por ID |
| `POST` | `/customers` | Crear cliente |
| `PUT` | `/customers/{id}` | Actualizar cliente |
| `DELETE` | `/customers/{id}` | Eliminar cliente (falla si tiene cuentas) |

### Cuentas

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/accounts/search/byCustomer?customerId={id}` | Cuentas por cliente |
| `GET` | `/accounts/{id}` | Obtener cuenta por ID |
| `GET` | `/accounts/{id}/balance` | Consultar saldo actual |
| `POST` | `/accounts` | Crear cuenta (`SAVINGS` o `CHECKING`) |
| `DELETE` | `/accounts/{id}` | Eliminar cuenta |

### Transacciones

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/transactions/search/byAccount?accountId={id}` | Transacciones por cuenta |
| `POST` | `/transactions/account/{accountId}` | Registrar transacción (`DEPOSIT` o `WITHDRAWAL`) |

### Documentación

| URL | Descripción |
|---|---|
| `http://localhost:9000/swagger-ui.html` | Swagger UI interactivo |
| `http://localhost:9000/api-docs` | OpenAPI JSON |

---

## Manejo de errores

Todos los errores devuelven un cuerpo JSON con estructura uniforme:

```json
{
  "status": 422,
  "error": "UNPROCESSABLE_ENTITY",
  "message": "Fondos insuficientes. Saldo actual: $200.00, monto solicitado: $500.00",
  "timestamp": "2025-04-09T10:30:00",
  "errors": []
}
```

| Código HTTP | Causa |
|---|---|
| `400 Bad Request` | Validación de campos (nulos, formato incorrecto) |
| `404 Not Found` | Recurso no encontrado |
| `409 Conflict` | Email o identificación ya registrados |
| `422 Unprocessable Entity` | Regla de negocio violada (menor de edad, fondos insuficientes, cuenta inactiva, etc.) |
| `500 Internal Server Error` | Error inesperado del servidor |

---

## Cómo correr localmente

### Requisitos previos

- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- Node.js 20+ y pnpm 9+

### 1. Base de datos

```sql
-- Crear la base de datos
CREATE DATABASE financial_db;

-- O ejecutar los scripts del directorio database/
psql -U postgres -d financial_db -f database/01_schema.sql
psql -U postgres -d financial_db -f database/02_seed.sql   -- opcional: datos de prueba
```

> Con `spring.jpa.hibernate.ddl-auto: update` el backend también puede crear las tablas automáticamente al arrancar.

### 2. Backend

```bash
cd backend

# Compilar y ejecutar
mvn spring-boot:run

# O compilar un JAR y ejecutarlo
mvn clean package -DskipTests
java -jar target/financial-system-0.0.1-SNAPSHOT.jar
```

El backend queda disponible en: `http://localhost:9000`

Variables de entorno opcionales (si difieren de los valores por defecto):

```bash
PGHOST=localhost PGPORT=5432 PGDATABASE=financial_db PGUSER=postgres PGPASSWORD=postgres \
  mvn spring-boot:run
```

### 3. Frontend

```bash
# Desde la raíz del monorepo
pnpm install
pnpm --filter @workspace/financial-frontend run dev
```

El frontend queda disponible en: `http://localhost:4200`  
Las llamadas a `/api/v1/*` son redirigidas al backend por el proxy configurado en `proxy.conf.json`.

---

## Tests y cobertura

### Ejecutar todos los tests

```bash
cd backend
mvn clean verify
```

### Resultados

| Métrica | Resultado | Umbral exigido |
|---|---|---|
| Tests totales | **173 pasando, 0 fallando** | — |
| Cobertura de líneas | **92.1 %** | ≥ 85 % |
| Cobertura de ramas | **80.4 %** | ≥ 80 % |

### Detalle por clase

| Clase | Líneas | Ramas |
|---|---|---|
| `CustomerServiceImpl` | 100 % | 86 % |
| `AccountServiceImpl` | 100 % | 100 % |
| `TransactionServiceImpl` | 98 % | 90 % |
| `CustomerEventHandler` | 100 % | 100 % |
| `GlobalExceptionHandler` | 77 % | 62 % |

### Estructura de tests

```
src/test/
├── integration/
│   ├── ServiceLayerIntegrationTest.java       # Servicios reales con H2 (23 tests)
│   ├── CustomerControllerIntegrationTest.java # MockMvc + @MockBean (11 tests)
│   ├── AccountControllerIntegrationTest.java  # MockMvc + @MockBean (10 tests)
│   ├── TransactionControllerIntegrationTest.java (11 tests)
│   └── GlobalExceptionHandlerTest.java        # Todos los handlers (19 tests)
└── unit/
    ├── entity/       # CustomerTest, AccountTest, TransactionTest (21 tests)
    ├── dto/          # Validación de DTOs con javax.validation (37 tests)
    ├── exception/    # ApiException, ApiResponse, ErrorResponse (11 tests)
    └── service/      # Mockito: Customer, Account, Transaction, EventHandler (30 tests)
```

El reporte HTML de cobertura se genera en `backend/target/site/jacoco/index.html`.

---

## Base de datos

### Scripts disponibles en `database/`

| Script | Descripción |
|---|---|
| `01_schema.sql` | Crea secuencias, tablas e índices (idempotente con `IF NOT EXISTS`) |
| `02_seed.sql` | Inserta datos de prueba: 4 clientes, 5 cuentas, 6 transacciones |

### Diagrama de tablas

```
customers
  id · first_name · last_name · identification_type · identification_number
  email · birth_date · created_at · updated_at

accounts
  id · account_number · account_type (SAVINGS|CHECKING) · status (ACTIVE|INACTIVE)
  balance · customer_id (FK→customers) · created_at · updated_at

transactions
  id · transaction_type (DEPOSIT|WITHDRAWAL) · amount · balance_after
  description · account_id (FK→accounts) · created_at
```

---

## Variables de entorno

### Backend

| Variable | Por defecto | Descripción |
|---|---|---|
| `PGHOST` | `localhost` | Host de PostgreSQL |
| `PGPORT` | `5432` | Puerto de PostgreSQL |
| `PGDATABASE` | `financial_db` | Nombre de la base de datos |
| `PGUSER` | `postgres` | Usuario de PostgreSQL |
| `PGPASSWORD` | `postgres` | Contraseña de PostgreSQL |
| `SERVER_PORT` | `9000` | Puerto del servidor Spring Boot |

### Frontend (solo en build de producción)

| Variable | Descripción |
|---|---|
| `API_URL` | URL absoluta del backend, ej: `https://financial-api.onrender.com` |

---

## Despliegue en Render

El proyecto incluye tres archivos `render.yaml`:

| Archivo | Uso |
|---|---|
| `render.yaml` | Despliegue **combinado** (BD + backend + frontend de una sola vez) |
| `backend/render.yaml` | Despliegue **solo del backend** (incluye BD PostgreSQL) |
| `artifacts/financial-frontend/render.yaml` | Despliegue **solo del frontend** |

### Flujo recomendado (despliegue separado)

**Paso 1 — Base de datos**

En Render → **New → PostgreSQL**. Luego copia las credenciales de conexión y ejecuta los scripts localmente:

```bash
psql "postgresql://<user>:<pass>@<host>/<db>" -f database/01_schema.sql
```

**Paso 2 — Backend**

- Render → **New → Blueprint**
- Blueprint File Path: `backend/render.yaml`
- Render conecta automáticamente las variables `PG*` a la base de datos
- Al finalizar, copia la URL generada (ej: `https://financial-api.onrender.com`)

**Paso 3 — Frontend**

- Render → **New → Blueprint**
- Blueprint File Path: `artifacts/financial-frontend/render.yaml`
- Agrega la variable de entorno `API_URL` = URL del backend del paso anterior

### Variables de entorno en Render

| Servicio | Variable | Valor |
|---|---|---|
| Backend | `PGHOST` | desde la BD de Render (automático con Blueprint) |
| Backend | `PGPORT` | desde la BD de Render (automático con Blueprint) |
| Backend | `PGDATABASE` | desde la BD de Render (automático con Blueprint) |
| Backend | `PGUSER` | desde la BD de Render (automático con Blueprint) |
| Backend | `PGPASSWORD` | desde la BD de Render (automático con Blueprint) |
| Backend | `SERVER_PORT` | `9000` |
| Frontend | `API_URL` | URL del backend en Render |

> **Plan gratuito de Render:** el backend se duerme tras 15 min de inactividad. La primera petición puede tardar ~30 s. La base de datos gratuita expira a los 90 días.

---

## Convenciones de commits

```
feat:     nueva funcionalidad
fix:      corrección de bug
refactor: refactorización sin cambio de comportamiento
test:     adición o modificación de tests
docs:     cambios en documentación
chore:    tareas de mantenimiento (deps, config)
```
