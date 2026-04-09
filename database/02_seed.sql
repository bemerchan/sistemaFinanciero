-- =============================================================
-- Mini Sistema Financiero - Flypass
-- Script 02: Datos de prueba (seed)
-- Ejecutar DESPUÉS de 01_schema.sql
-- Ejecutar como: psql -U <user> -d <database> -f 02_seed.sql
-- =============================================================

-- -------------------------------------------------------------
-- Clientes de ejemplo
-- -------------------------------------------------------------
INSERT INTO customers (first_name, last_name, identification_type, identification_number,
                       email, birth_date, created_at)
VALUES
    ('Ana',     'García',    'CC',       '1234567890', 'ana.garcia@email.com',    '1990-03-15', NOW()),
    ('Carlos',  'Martínez',  'CC',       '9876543210', 'carlos.martinez@email.com','1985-07-22', NOW()),
    ('Laura',   'Rodríguez', 'CE',       'CE-456789',  'laura.rodriguez@email.com','1995-11-08', NOW()),
    ('Miguel',  'López',     'PASSPORT', 'AB1234567',  'miguel.lopez@email.com',  '1988-01-30', NOW())
ON CONFLICT DO NOTHING;

-- -------------------------------------------------------------
-- Cuentas de ejemplo
-- Ahorro   : 53XXXXXXXX  (prefijo 53 + 8 dígitos)
-- Corriente: 33XXXXXXXX  (prefijo 33 + 8 dígitos)
-- -------------------------------------------------------------
INSERT INTO accounts (account_number, account_type, status, balance, customer_id, created_at)
VALUES
    ('5300000001', 'SAVINGS',  'ACTIVE',   1500000.00, 1, NOW()),
    ('3300000001', 'CHECKING', 'ACTIVE',    250000.00, 1, NOW()),
    ('5300000002', 'SAVINGS',  'ACTIVE',   3200000.00, 2, NOW()),
    ('5300000003', 'SAVINGS',  'INACTIVE',       0.00, 3, NOW()),
    ('3300000002', 'CHECKING', 'ACTIVE',    780000.00, 4, NOW())
ON CONFLICT DO NOTHING;

-- Ajustar las secuencias de números de cuenta para que no colisionen
SELECT setval('savings_account_seq',  3, true);
SELECT setval('checking_account_seq', 2, true);

-- -------------------------------------------------------------
-- Transacciones de ejemplo
-- -------------------------------------------------------------
INSERT INTO transactions (transaction_type, amount, balance_after, description, account_id, created_at)
VALUES
    ('DEPOSIT',    2000000.00, 2000000.00, 'Depósito inicial',          1, NOW() - INTERVAL '30 days'),
    ('WITHDRAWAL',  500000.00, 1500000.00, 'Retiro cajero',             1, NOW() - INTERVAL '15 days'),
    ('DEPOSIT',     500000.00,  500000.00, 'Depósito nómina',           2, NOW() - INTERVAL '20 days'),
    ('WITHDRAWAL',  250000.00,  250000.00, 'Pago servicios',            2, NOW() - INTERVAL '5 days'),
    ('DEPOSIT',    3200000.00, 3200000.00, 'Transferencia recibida',    3, NOW() - INTERVAL '10 days'),
    ('DEPOSIT',     780000.00,  780000.00, 'Abono inicial',             5, NOW() - INTERVAL '7 days')
ON CONFLICT DO NOTHING;

-- Ajustar secuencias de IDs
SELECT setval('customers_id_seq',    (SELECT MAX(id) FROM customers));
SELECT setval('accounts_id_seq',     (SELECT MAX(id) FROM accounts));
SELECT setval('transactions_id_seq', (SELECT MAX(id) FROM transactions));
