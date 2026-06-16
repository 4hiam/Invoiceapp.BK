-- ============================================================
-- Fix: las tablas de ítems extienden BaseEntity (created_at)
-- pero V1 las creó sin la columna, rompiendo ddl-auto: validate.
-- ============================================================

ALTER TABLE invoice_items
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW();

ALTER TABLE recurring_invoice_items
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW();

ALTER TABLE budget_items
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW();
