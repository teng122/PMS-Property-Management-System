CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE invoices (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id      UUID            NOT NULL,
    room_charge     DECIMAL(12,2)   NOT NULL DEFAULT 0,
    service_charge  DECIMAL(12,2)   NOT NULL DEFAULT 0,
    tax             DECIMAL(12,2)   NOT NULL DEFAULT 0,
    total_amount    DECIMAL(12,2)   NOT NULL DEFAULT 0,
    status          VARCHAR(20)     NOT NULL DEFAULT 'UNPAID',
    created_at      TIMESTAMP       NOT NULL DEFAULT now(),
    paid_at         TIMESTAMP
);

CREATE UNIQUE INDEX uq_invoices_booking ON invoices (booking_id);
