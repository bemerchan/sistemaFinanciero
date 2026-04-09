-- =============================================================
-- Mini Sistema Financiero - Flypass
-- Script 01: Creación del esquema
-- Motor: PostgreSQL 14+
-- Ejecutar como: psql -U <user> -d <database> -f 01_schema.sql
-- =============================================================

-- -------------------------------------------------------------
-- SECUENCIAS DE IDs (usadas por Hibernate / JPA)
-- -------------------------------------------------------------
CREATE SEQUENCE IF NOT EXISTS customers_id_seq
    START WITH 1 INCREMENT BY 1 NO MAXVALUE NO CYCLE;

CREATE SEQUENCE IF NOT EXISTS accounts_id_seq
    START WITH 1 INCREMENT BY 1 NO MAXVALUE NO CYCLE;

CREATE SEQUENCE IF NOT EXISTS transactions_id_seq
    START WITH 1 INCREMENT BY 1 NO MAXVALUE NO CYCLE;

-- -------------------------------------------------------------
-- SECUENCIAS DE NÚMEROS DE CUENTA
--   Ahorro   : prefijo "53" + LPAD(nextval, 8, '0')  → 53XXXXXXXX
--   Corriente: prefijo "33" + LPAD(nextval, 8, '0')  → 33XXXXXXXX
-- -------------------------------------------------------------
CREATE SEQUENCE IF NOT EXISTS savings_account_seq
    START WITH 1 INCREMENT BY 1 NO MAXVALUE NO CYCLE;

CREATE SEQUENCE IF NOT EXISTS checking_account_seq
    START WITH 1 INCREMENT BY 1 NO MAXVALUE NO CYCLE;

-- -------------------------------------------------------------
-- TABLA: customers
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customers (
    id                    BIGINT        NOT NULL DEFAULT nextval('customers_id_seq'),
    first_name            VARCHAR(100)  NOT NULL,
    last_name             VARCHAR(100)  NOT NULL,
    identification_type   VARCHAR(20)   NOT NULL,
    identification_number VARCHAR(50)   NOT NULL,
    email                 VARCHAR(150)  NOT NULL,
    birth_date            DATE          NOT NULL,
    created_at            TIMESTAMP(6)  NOT NULL,
    updated_at            TIMESTAMP(6),

    CONSTRAINT customers_pkey
        PRIMARY KEY (id),

    CONSTRAINT customers_email_uk
        UNIQUE (email),

    CONSTRAINT customers_identification_number_uk
        UNIQUE (identification_number),

    CONSTRAINT customers_identification_type_check
        CHECK (identification_type IN ('CC', 'CE', 'TI', 'PASSPORT', 'NIT'))
);

-- -------------------------------------------------------------
-- TABLA: accounts
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS accounts (
    id             BIGINT        NOT NULL DEFAULT nextval('accounts_id_seq'),
    account_number VARCHAR(10)   NOT NULL,
    account_type   VARCHAR(20)   NOT NULL,
    status         VARCHAR(10)   NOT NULL DEFAULT 'ACTIVE',
    balance        NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    customer_id    BIGINT        NOT NULL,
    created_at     TIMESTAMP(6)  NOT NULL,
    updated_at     TIMESTAMP(6),

    CONSTRAINT accounts_pkey
        PRIMARY KEY (id),

    CONSTRAINT accounts_account_number_uk
        UNIQUE (account_number),

    CONSTRAINT accounts_account_type_check
        CHECK (account_type IN ('SAVINGS', 'CHECKING')),

    CONSTRAINT accounts_status_check
        CHECK (status IN ('ACTIVE', 'INACTIVE')),

    CONSTRAINT accounts_customer_fk
        FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- -------------------------------------------------------------
-- TABLA: transactions
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS transactions (
    id               BIGINT        NOT NULL DEFAULT nextval('transactions_id_seq'),
    transaction_type VARCHAR(20)   NOT NULL,
    amount           NUMERIC(15,2) NOT NULL,
    balance_after    NUMERIC(15,2) NOT NULL,
    description      VARCHAR(255),
    account_id       BIGINT        NOT NULL,
    created_at       TIMESTAMP(6)  NOT NULL,

    CONSTRAINT transactions_pkey
        PRIMARY KEY (id),

    CONSTRAINT transactions_transaction_type_check
        CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAWAL')),

    CONSTRAINT transactions_account_fk
        FOREIGN KEY (account_id) REFERENCES accounts(id)
);

-- -------------------------------------------------------------
-- ÍNDICES de rendimiento
-- -------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_accounts_customer_id
    ON accounts (customer_id);

CREATE INDEX IF NOT EXISTS idx_transactions_account_id
    ON transactions (account_id);

CREATE INDEX IF NOT EXISTS idx_transactions_created_at
    ON transactions (created_at DESC);
