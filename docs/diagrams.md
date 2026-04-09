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

## 2. Manejo de errores

```mermaid
flowchart LR
    REQ(["Petición HTTP"])

    subgraph F1["① Filtro CORS"]
        CORS{"¿Origen\npermitido?"}
    end

    subgraph F2["② Validación Bean"]
        VALID{"@Valid\npasa?"}
    end

    subgraph F3["③ Regla de negocio"]
        BIZ{"Servicio"}
    end

    subgraph RESP["Respuesta"]
        OK["✅ 200 / 201\n{ message, data }"]
        E403["🚫 403 Forbidden\nOrigen bloqueado"]
        E400["⚠️ 400 Bad Request\nCampos inválidos o\nJSON malformado"]
        E404["🔍 404 Not Found\nRecurso inexistente"]
        E409["🔁 409 Conflict\nEmail o ID duplicado"]
        E422["❌ 422 Unprocessable\nEdad menor de 18 ·\nCuenta inactiva ·\nFondos insuficientes"]
        E500["💥 500 Server Error\nError inesperado"]
    end

    REQ --> CORS
    CORS -- "No" --> E403
    CORS -- "Sí" --> VALID
    VALID -- "No" --> E400
    VALID -- "Sí" --> BIZ
    BIZ -- "OK" --> OK
    BIZ -- "no existe" --> E404
    BIZ -- "duplicado" --> E409
    BIZ -- "regla incumplida" --> E422
    BIZ -- "excepción" --> E500

    style OK   fill:#dcfce7,stroke:#16a34a,color:#14532d
    style E403 fill:#f1f5f9,stroke:#64748b,color:#334155
    style E400 fill:#fee2e2,stroke:#ef4444,color:#7f1d1d
    style E404 fill:#fef3c7,stroke:#d97706,color:#78350f
    style E409 fill:#fce7f3,stroke:#db2777,color:#831843
    style E422 fill:#ede9fe,stroke:#7c3aed,color:#3b0764
    style E500 fill:#1e293b,stroke:#0f172a,color:#f8fafc
```

> **Todos los errores** pasan por `GlobalExceptionHandler`, que normaliza la respuesta en el formato:
> ```json
> { "status": 422, "error": "Unprocessable Entity", "message": "...", "timestamp": "..." }
> ```

---

## 3. Diagrama entidad-relación (BD)

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

## 4. Ciclo de vida de una transacción

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
