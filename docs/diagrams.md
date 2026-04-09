# Diagramas — Mini Sistema Financiero Flypass

---

## 1. Arquitectura de la solución

```mermaid
graph TB
    subgraph BROWSER["🌐 Navegador"]
        direction TB
        A1[Angular 18 SPA]
        A2[Angular Material UI]
        A3[Reactive Forms]
        A4[Angular Router\nLazy Loading]
    end

    subgraph BACKEND["☕ Spring Boot 3.2 — Puerto 9000"]
        direction TB
        B1[Spring Data REST\n/api/v1]
        B2[CustomerController\nAccountController\nTransactionController]
        B3[CustomerService\nAccountService\nTransactionService]
        B4[Spring Data JPA\nRepositories]
        B5[GlobalExceptionHandler\nAOP]
    end

    subgraph DB["🗄️ PostgreSQL"]
        direction TB
        D1[(customers)]
        D2[(accounts)]
        D3[(transactions)]
    end

    subgraph RENDER["☁️ Render — Producción"]
        direction LR
        R1[Static Site\nfrontend]
        R2[Web Service\nDocker]
        R3[Managed PostgreSQL]
    end

    A1 -->|HTTP + CORS| B1
    A2 --> A1
    A3 --> A1
    A4 --> A1
    B1 --> B2
    B2 --> B3
    B3 --> B4
    B5 -.->|intercepta errores| B2
    B4 -->|JDBC| D1
    B4 -->|JDBC| D2
    B4 -->|JDBC| D3

    R1 -.->|despliega| A1
    R2 -.->|despliega| B1
    R3 -.->|aloja| D1

    style BROWSER fill:#eff6ff,stroke:#3b82f6,color:#1e293b
    style BACKEND fill:#f0fdf4,stroke:#16a34a,color:#1e293b
    style DB      fill:#fef9c3,stroke:#ca8a04,color:#1e293b
    style RENDER  fill:#f5f3ff,stroke:#7c3aed,color:#1e293b
```

---

## 2. Flujo principal de la aplicación

```mermaid
sequenceDiagram
    actor U as Usuario
    participant FE as Angular Frontend
    participant API as Spring Boot API
    participant DB as PostgreSQL

    %% ── Ver clientes ───────────────────────────────────────
    U->>FE: Abre la aplicación
    FE->>API: GET /api/v1/customers?size=200&sort=id,desc
    API->>DB: SELECT * FROM customers
    DB-->>API: Lista de clientes
    API-->>FE: 200 OK · { _embedded: { customers: [...] } }
    FE-->>U: Muestra tabla de clientes

    %% ── Crear cliente ──────────────────────────────────────
    U->>FE: Clic "Nuevo cliente" → completa formulario
    FE->>API: POST /api/v1/customers · { firstName, email, birthDate... }
    API->>API: Valida edad ≥ 18 · email único · identificación única
    API->>DB: INSERT INTO customers
    DB-->>API: Cliente creado (id, created_at)
    API-->>FE: 201 Created · { message, data: { id, ... } }
    FE-->>U: Snackbar "Cliente creado" · recarga tabla

    %% ── Ver detalle ────────────────────────────────────────
    U->>FE: Clic "Ver detalle" del cliente
    FE->>API: GET /api/v1/accounts/search/byCustomer?customerId={id}
    API->>DB: SELECT * FROM accounts WHERE customer_id = ?
    DB-->>API: Lista de cuentas
    API-->>FE: 200 OK · { _embedded: { accounts: [...] } }
    FE-->>U: Muestra cuentas del cliente

    %% ── Crear cuenta ───────────────────────────────────────
    U->>FE: Clic "Nueva cuenta" → elige tipo SAVINGS / CHECKING
    FE->>API: POST /api/v1/accounts · { accountType, customerId }
    API->>DB: SELECT nextval('savings_account_seq')
    API->>DB: INSERT INTO accounts (account_number "53XXXXXXXX", balance=0)
    API-->>FE: 201 Created · { data: { accountNumber, balance } }
    FE-->>U: Muestra la nueva cuenta en el panel

    %% ── Registrar transacción ──────────────────────────────
    U->>FE: Selecciona DEPOSIT/WITHDRAWAL · ingresa monto
    FE->>API: POST /api/v1/transactions/account/{id} · { transactionType, amount }
    API->>API: Verifica cuenta ACTIVE · valida fondos suficientes
    API->>DB: UPDATE accounts SET balance = balance ± amount
    API->>DB: INSERT INTO transactions (amount, balance_after)
    API-->>FE: 201 Created · { data: { balanceAfter } }
    FE-->>U: Panel actualiza saldo · lista de movimientos
```

---

## 3. Flujo de manejo de errores

```mermaid
flowchart TD
    REQ([Petición HTTP])
    REQ --> CORS{¿Pasa\nCorsFilter?}
    CORS -->|No — origen bloqueado| E_CORS[403 Forbidden]
    CORS -->|Sí| VALID

    VALID{¿Validación\n@Valid pasa?}
    VALID -->|No — campos inválidos| E400[400 Bad Request\nMethodArgumentNotValid\n→ lista de field errors]
    VALID -->|No — body malformado| E400B[400 Bad Request\nHttpMessageNotReadable\n→ JSON inválido / enum desconocido]
    VALID -->|Sí| BIZ

    BIZ{Regla\nde negocio}

    BIZ -->|Cliente < 18 años| E422A[422 Unprocessable Entity\n'No se permite registrar\nclientes menores de 18 años']
    BIZ -->|Email ya registrado| E409A[409 Conflict\n'Ya existe un cliente con\nese correo electrónico']
    BIZ -->|ID duplicada| E409B[409 Conflict\n'Ya existe un cliente con\nesa identificación']
    BIZ -->|Cliente con cuentas\n→ intento de borrar| E422B[422 Unprocessable Entity\n'No se puede eliminar un cliente\nque tiene cuentas asociadas']
    BIZ -->|Recurso no existe| E404[404 Not Found\n'Cliente / Cuenta no encontrada']
    BIZ -->|Cuenta INACTIVE\n→ transacción| E422C[422 Unprocessable Entity\n'La cuenta está inactiva']
    BIZ -->|Fondos insuficientes| E422D[422 Unprocessable Entity\n'Fondos insuficientes.\nSaldo actual: $X, solicitado: $Y']
    BIZ -->|OK| OK[200 / 201 Success\n{ message, data }]

    ERR_UNEXP{¿Error\ninesperado?}
    E422A & E409A & E409B & E422B & E404 & E422C & E422D --> HANDLER
    HANDLER[GlobalExceptionHandler\nregistra en log · formatea respuesta]
    HANDLER --> RESP[JSON uniforme\n{ status, error, message, timestamp, errors }]

    REQ -->|RuntimeException no\ncontrolada| ERR_UNEXP
    ERR_UNEXP --> E500[500 Internal Server Error\n'Error inesperado']
    E500 --> RESP

    style OK     fill:#dcfce7,stroke:#16a34a,color:#14532d
    style E400   fill:#fee2e2,stroke:#ef4444,color:#7f1d1d
    style E400B  fill:#fee2e2,stroke:#ef4444,color:#7f1d1d
    style E404   fill:#fef3c7,stroke:#d97706,color:#78350f
    style E409A  fill:#fce7f3,stroke:#db2777,color:#831843
    style E409B  fill:#fce7f3,stroke:#db2777,color:#831843
    style E422A  fill:#ede9fe,stroke:#7c3aed,color:#3b0764
    style E422B  fill:#ede9fe,stroke:#7c3aed,color:#3b0764
    style E422C  fill:#ede9fe,stroke:#7c3aed,color:#3b0764
    style E422D  fill:#ede9fe,stroke:#7c3aed,color:#3b0764
    style E500   fill:#1e293b,stroke:#0f172a,color:#f8fafc
    style E_CORS fill:#f1f5f9,stroke:#64748b,color:#334155
```

---

## 4. Diagrama entidad-relación (BD)

```mermaid
erDiagram
    CUSTOMERS {
        bigint   id                    PK
        varchar  first_name            "NOT NULL"
        varchar  last_name             "NOT NULL"
        varchar  identification_type   "CC|CE|TI|PASSPORT|NIT"
        varchar  identification_number "UNIQUE NOT NULL"
        varchar  email                 "UNIQUE NOT NULL"
        date     birth_date            "NOT NULL · edad ≥ 18"
        timestamp created_at
        timestamp updated_at
    }

    ACCOUNTS {
        bigint   id             PK
        varchar  account_number "UNIQUE · 53XXXXXXXX | 33XXXXXXXX"
        varchar  account_type   "SAVINGS | CHECKING"
        varchar  status         "ACTIVE | INACTIVE"
        numeric  balance        "≥ 0.00"
        bigint   customer_id    FK
        timestamp created_at
        timestamp updated_at
    }

    TRANSACTIONS {
        bigint   id               PK
        varchar  transaction_type "DEPOSIT | WITHDRAWAL"
        numeric  amount           "> 0.01"
        numeric  balance_after
        varchar  description      "opcional"
        bigint   account_id       FK
        timestamp created_at
    }

    CUSTOMERS ||--o{ ACCOUNTS     : "tiene"
    ACCOUNTS  ||--o{ TRANSACTIONS : "registra"
```

---

## 5. Ciclo de vida de una transacción

```mermaid
stateDiagram-v2
    [*] --> Recibida : POST /transactions/account/{id}

    Recibida --> ValidandoCuenta : verifica existencia
    ValidandoCuenta --> CuentaNoEncontrada : 404 Not Found
    ValidandoCuenta --> ValidandoEstado : cuenta existe

    ValidandoEstado --> CuentaInactiva : status = INACTIVE
    ValidandoEstado --> ValidandoFondos : status = ACTIVE

    ValidandoFondos --> FondosInsuficientes : WITHDRAWAL y balance < amount
    ValidandoFondos --> ActualizandoSaldo : fondos OK

    ActualizandoSaldo --> RegistrandoMovimiento : UPDATE accounts SET balance
    RegistrandoMovimiento --> Completada : INSERT transactions

    Completada --> [*] : 201 Created · { balanceAfter }
    CuentaNoEncontrada --> [*] : 404 Not Found
    CuentaInactiva --> [*] : 422 Unprocessable Entity
    FondosInsuficientes --> [*] : 422 Unprocessable Entity

    state ActualizandoSaldo {
        [*] --> Deposito : DEPOSIT
        [*] --> Retiro    : WITHDRAWAL
        Deposito  : balance = balance + amount
        Retiro    : balance = balance - amount
    }
```
