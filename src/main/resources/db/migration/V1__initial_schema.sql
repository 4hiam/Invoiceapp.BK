-- V1__initial_schema.sql
-- Invoice App - Initial database schema

-- ============================================================
-- USERS
-- ============================================================
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    name            VARCHAR(100) NOT NULL,
    phone           VARCHAR(20),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);

-- ============================================================
-- COMPANIES
-- ============================================================
CREATE TABLE companies (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,
    logo_url        VARCHAR(500),
    address         TEXT,
    tax_id          VARCHAR(50),
    currency        VARCHAR(3) NOT NULL DEFAULT 'ARS',
    timezone        VARCHAR(50) NOT NULL DEFAULT 'America/Argentina/Jujuy',
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_companies_user_id ON companies(user_id);

-- ============================================================
-- CLIENTS
-- ============================================================
CREATE TABLE clients (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id      UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,
    email           VARCHAR(255),
    phone           VARCHAR(20),
    address         TEXT,
    tax_id          VARCHAR(50),
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_clients_company_id ON clients(company_id);

-- ============================================================
-- PRODUCTS
-- ============================================================
CREATE TABLE products (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id      UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    name            VARCHAR(200) NOT NULL,
    description     TEXT,
    price           DECIMAL(12,2) NOT NULL,
    vat_rate        DECIMAL(5,2) NOT NULL DEFAULT 21.0,
    unit            VARCHAR(20) NOT NULL DEFAULT 'unidad',
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_products_company_id ON products(company_id);

-- ============================================================
-- RECURRING INVOICES (before invoices, because invoices references it)
-- ============================================================
CREATE TABLE recurring_invoices (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id                  UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    client_id                   UUID NOT NULL REFERENCES clients(id),
    frequency                   VARCHAR(20) NOT NULL, -- monthly, weekly, biweekly, annual
    day_of_month                INTEGER NOT NULL CHECK (day_of_month BETWEEN 1 AND 28),
    start_date                  DATE NOT NULL,
    end_date                    DATE,
    is_active                   BOOLEAN NOT NULL DEFAULT TRUE,
    auto_send                   BOOLEAN NOT NULL DEFAULT FALSE,
    last_notification_status    VARCHAR(20) DEFAULT 'pending', -- pending, sent, confirmed, cancelled, expired
    next_generation_date        TIMESTAMP NOT NULL,
    created_at                  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_recurring_company_id ON recurring_invoices(company_id);
CREATE INDEX idx_recurring_next_gen ON recurring_invoices(next_generation_date, is_active);

-- ============================================================
-- INVOICES
-- ============================================================
CREATE TABLE invoices (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id              UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    client_id               UUID NOT NULL REFERENCES clients(id),
    recurring_invoice_id    UUID REFERENCES recurring_invoices(id),
    is_recurring_source     BOOLEAN NOT NULL DEFAULT FALSE,
    invoice_number          VARCHAR(20) NOT NULL,
    status                  VARCHAR(20) NOT NULL DEFAULT 'draft', -- draft, sent, paid, overdue, cancelled
    issue_date              DATE NOT NULL,
    due_date                DATE NOT NULL,
    subtotal                DECIMAL(12,2) NOT NULL DEFAULT 0,
    vat_total               DECIMAL(12,2) NOT NULL DEFAULT 0,
    discount                DECIMAL(12,2) NOT NULL DEFAULT 0,
    total                   DECIMAL(12,2) NOT NULL DEFAULT 0,
    amount_paid             DECIMAL(12,2) NOT NULL DEFAULT 0,
    payment_terms           VARCHAR(50),
    notes                   TEXT,
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(company_id, invoice_number)
);

CREATE INDEX idx_invoices_company_id ON invoices(company_id);
CREATE INDEX idx_invoices_client_id ON invoices(client_id);
CREATE INDEX idx_invoices_status ON invoices(company_id, status);
CREATE INDEX idx_invoices_recurring ON invoices(recurring_invoice_id);

-- ============================================================
-- INVOICE ITEMS
-- ============================================================
CREATE TABLE invoice_items (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_id      UUID NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    product_id      UUID REFERENCES products(id),
    description     VARCHAR(500) NOT NULL,
    quantity        INTEGER NOT NULL,
    unit_price      DECIMAL(12,2) NOT NULL,
    vat_rate        DECIMAL(5,2) NOT NULL,
    total           DECIMAL(12,2) NOT NULL
);

CREATE INDEX idx_invoice_items_invoice_id ON invoice_items(invoice_id);

-- ============================================================
-- RECURRING INVOICE ITEMS (snapshot)
-- ============================================================
CREATE TABLE recurring_invoice_items (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recurring_invoice_id    UUID NOT NULL REFERENCES recurring_invoices(id) ON DELETE CASCADE,
    product_id              UUID REFERENCES products(id),
    description             VARCHAR(500) NOT NULL,
    quantity                INTEGER NOT NULL,
    unit_price              DECIMAL(12,2) NOT NULL,
    vat_rate                DECIMAL(5,2) NOT NULL
);

CREATE INDEX idx_rec_items_recurring_id ON recurring_invoice_items(recurring_invoice_id);

-- ============================================================
-- BUDGETS
-- ============================================================
CREATE TABLE budgets (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id      UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    client_id       UUID NOT NULL REFERENCES clients(id),
    budget_number   VARCHAR(20) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'draft', -- draft, sent, accepted, rejected
    issue_date      DATE NOT NULL,
    valid_until     DATE NOT NULL,
    subtotal        DECIMAL(12,2) NOT NULL DEFAULT 0,
    vat_total       DECIMAL(12,2) NOT NULL DEFAULT 0,
    discount        DECIMAL(12,2) NOT NULL DEFAULT 0,
    total           DECIMAL(12,2) NOT NULL DEFAULT 0,
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(company_id, budget_number)
);

CREATE INDEX idx_budgets_company_id ON budgets(company_id);

-- ============================================================
-- BUDGET ITEMS
-- ============================================================
CREATE TABLE budget_items (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    budget_id       UUID NOT NULL REFERENCES budgets(id) ON DELETE CASCADE,
    product_id      UUID REFERENCES products(id),
    description     VARCHAR(500) NOT NULL,
    quantity        INTEGER NOT NULL,
    unit_price      DECIMAL(12,2) NOT NULL,
    vat_rate        DECIMAL(5,2) NOT NULL,
    total           DECIMAL(12,2) NOT NULL
);

CREATE INDEX idx_budget_items_budget_id ON budget_items(budget_id);

-- ============================================================
-- EXPENSES
-- ============================================================
CREATE TABLE expenses (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id      UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    category        VARCHAR(50) NOT NULL,
    description     VARCHAR(500) NOT NULL,
    amount          DECIMAL(12,2) NOT NULL,
    tax_amount      DECIMAL(12,2) NOT NULL DEFAULT 0,
    expense_date    DATE NOT NULL,
    receipt_url     VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_expenses_company_id ON expenses(company_id);
CREATE INDEX idx_expenses_date ON expenses(company_id, expense_date);

-- ============================================================
-- PAYMENTS
-- ============================================================
CREATE TABLE payments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_id      UUID NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    amount          DECIMAL(12,2) NOT NULL,
    payment_date    DATE NOT NULL,
    payment_method  VARCHAR(50),
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payments_invoice_id ON payments(invoice_id);

-- ============================================================
-- NOTIFICATIONS
-- ============================================================
CREATE TABLE notifications (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    company_id      UUID REFERENCES companies(id),
    type            VARCHAR(30) NOT NULL, -- recurring_reminder, payment, system
    title           VARCHAR(200) NOT NULL,
    body            TEXT NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'pending', -- pending, sent, confirmed, cancelled, expired
    action_url      VARCHAR(500),
    is_read         BOOLEAN NOT NULL DEFAULT FALSE,
    sent_at         TIMESTAMP,
    read_at         TIMESTAMP
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id, is_read);

-- ============================================================
-- TIME ENTRIES
-- ============================================================
CREATE TABLE time_entries (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id          UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    client_id           UUID NOT NULL REFERENCES clients(id),
    description         VARCHAR(500) NOT NULL,
    duration_minutes    INTEGER NOT NULL,
    hourly_rate         DECIMAL(12,2),
    entry_date          DATE NOT NULL,
    is_billed           BOOLEAN NOT NULL DEFAULT FALSE,
    invoice_id          UUID REFERENCES invoices(id),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_time_entries_company_id ON time_entries(company_id);
CREATE INDEX idx_time_entries_client_id ON time_entries(client_id);

-- ============================================================
-- FCM TOKENS (for push notifications)
-- ============================================================
CREATE TABLE fcm_tokens (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token           VARCHAR(500) NOT NULL,
    device_info     VARCHAR(200),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, token)
);

CREATE INDEX idx_fcm_tokens_user_id ON fcm_tokens(user_id);
