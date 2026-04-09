# Mini Sistema Financiero - Flypass

Aplicación web full-stack para gestión de clientes y cuentas bancarias, desarrollada como prueba técnica para Flypass.

## Stack Tecnológico

### Backend — `backend/`
- **Java 17** + **Spring Boot 3.2.5**
- **Spring Data JPA** + Hibernate (ORM)
- **PostgreSQL** como base de datos
- **SpringDoc OpenAPI 2.x** — documentación Swagger
- **Lombok** para reducir código repetitivo
- **Maven** como gestor de dependencias

### Frontend — `artifacts/financial-frontend/`
- **Angular 18** (standalone components, lazy-loaded routes)
- **Angular Material 18** (tema azure-blue, M3)
- **ReactiveFormsModule** (FormGroup, FormBuilder, Validators)
- Proxy `/api/v1` → backend en puerto 9000

## Arquitectura del Backend

```
src/main/java/com/flypass/financial/
├── config/           # Configuración (CORS, JPA, Swagger)
├── controller/       # Controladores REST
├── dto/
│   ├── request/      # DTOs de entrada
│   └── response/     # DTOs de salida
├── entity/           # Entidades JPA
├── exception/        # Excepciones personalizadas + GlobalExceptionHandler
├── repository/       # Interfaces Spring Data JPA
└── service/
    └── impl/         # Implementaciones de servicios
```

## Requisitos Previos

- Java 17+
- Maven 3.8+
- PostgreSQL 13+

## Configuración de Base de Datos

Crear la base de datos en PostgreSQL:

```sql
CREATE DATABASE financial_db;
CREATE USER postgres WITH PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE financial_db TO postgres;
```

## Variables de Entorno

Configurar las siguientes variables (o modificar `application.yml`):

| Variable | Valor por defecto | Descripción |
|----------|-------------------|-------------|
| `DB_HOST` | `localhost` | Host de PostgreSQL |
| `DB_PORT` | `5432` | Puerto de PostgreSQL |
| `DB_NAME` | `financial_db` | Nombre de la base de datos |
| `DB_USERNAME` | `postgres` | Usuario de la BD |
| `DB_PASSWORD` | `postgres` | Contraseña de la BD |
| `SERVER_PORT` | `8080` | Puerto del servidor |

## Cómo correr el Backend

```bash
cd backend

# Compilar
mvn clean compile

# Ejecutar
mvn spring-boot:run

# O con variables de entorno específicas
DB_HOST=localhost DB_PASSWORD=mipassword mvn spring-boot:run
```

La aplicación estará disponible en: `http://localhost:8080`

## Documentación API (Swagger)

Una vez corriendo el servidor, acceder a:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

## Endpoints Disponibles

### Clientes (`/api/v1/customers`)
| Método | Ruta | Descripción |
|--------|------|-------------|
| `POST` | `/api/v1/customers` | Crear cliente |
| `GET` | `/api/v1/customers` | Listar todos los clientes |
| `GET` | `/api/v1/customers/{id}` | Obtener cliente por ID |
| `PUT` | `/api/v1/customers/{id}` | Actualizar cliente |
| `DELETE` | `/api/v1/customers/{id}` | Eliminar cliente |

### Cuentas (`/api/v1/accounts`)
| Método | Ruta | Descripción |
|--------|------|-------------|
| `POST` | `/api/v1/accounts` | Crear cuenta bancaria |
| `GET` | `/api/v1/accounts/customer/{customerId}` | Cuentas por cliente |
| `GET` | `/api/v1/accounts/{id}` | Obtener cuenta por ID |
| `GET` | `/api/v1/accounts/{id}/balance` | Consultar saldo |

### Transacciones (`/api/v1/transactions`)
| Método | Ruta | Descripción |
|--------|------|-------------|
| `POST` | `/api/v1/transactions/account/{accountId}` | Registrar transacción |
| `GET` | `/api/v1/transactions/account/{accountId}` | Listar movimientos |
| `GET` | `/api/v1/transactions/account/{accountId}/last?limit=5` | Últimos N movimientos |

## Reglas de Negocio Implementadas

- ✅ No se permiten clientes menores de 18 años
- ✅ No se puede eliminar un cliente con cuentas vinculadas
- ✅ Cuentas de ahorro: prefijo `53XXXXXXXX`, saldo mínimo $0
- ✅ Cuentas corrientes: prefijo `33XXXXXXXX`
- ✅ Número de cuenta único de 10 dígitos, autogenerado
- ✅ Estado inicial de cuenta: ACTIVA
- ✅ Las transacciones actualizan el saldo automáticamente
- ✅ Fechas de creación y modificación automáticas

## Tipos de Identificación Soportados

`CC`, `CE`, `TI`, `PASSPORT`, `NIT`

## Manejo de Errores

El sistema maneja los siguientes códigos HTTP:
- `200 OK` — Operación exitosa
- `201 Created` — Recurso creado
- `400 Bad Request` — Datos de entrada inválidos
- `404 Not Found` — Recurso no encontrado
- `409 Conflict` — Datos duplicados (email o identificación)
- `422 Unprocessable Entity` — Regla de negocio violada (menor de edad, fondos insuficientes, etc.)
- `500 Internal Server Error` — Error inesperado del servidor

## Atajos Tomados por Tiempo

- Los tests unitarios se agregarán en una siguiente iteración
- La seguridad (Spring Security / JWT) no está implementada ya que no era requerida en la prueba
- Las cuentas corrientes también tienen saldo mínimo de $0 (aunque podrían tener sobregiro en un sistema real)

## Commits

El repositorio utiliza commits progresivos por funcionalidad siguiendo el estándar:
- `feat:` nuevas funcionalidades
- `fix:` correcciones de bugs
- `refactor:` refactorizaciones
- `docs:` documentación
- `test:` tests unitarios
