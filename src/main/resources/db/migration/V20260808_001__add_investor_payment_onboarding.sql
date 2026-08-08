ALTER TABLE investor_account
    ALTER COLUMN status TYPE VARCHAR(30);

ALTER TABLE app_setting
    ALTER COLUMN setting_value TYPE TEXT;

CREATE TABLE IF NOT EXISTS investor_payment (
    id BIGSERIAL PRIMARY KEY,
    investor_account_id BIGINT NOT NULL REFERENCES investor_account(id),
    investment_id BIGINT NOT NULL UNIQUE REFERENCES investment(id),
    source_inquiry_id BIGINT NOT NULL UNIQUE REFERENCES inquiry(id),
    merchant_link_id VARCHAR(80) NOT NULL UNIQUE,
    provider_link_id VARCHAR(120),
    link_url VARCHAR(1200),
    amount NUMERIC(14, 2) NOT NULL,
    amount_paid NUMERIC(14, 2) NOT NULL DEFAULT 0,
    currency VARCHAR(8) NOT NULL DEFAULT 'INR',
    status VARCHAR(30) NOT NULL,
    link_expires_at TIMESTAMP,
    payment_reference VARCHAR(160),
    paid_at TIMESTAMP,
    email_delivery_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    email_sent_at TIMESTAMP,
    email_error VARCHAR(600),
    portal_invite_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    portal_invite_sent_at TIMESTAMP,
    portal_invite_error VARCHAR(600),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS investor_payment_event (
    id BIGSERIAL PRIMARY KEY,
    investor_payment_id BIGINT NOT NULL REFERENCES investor_payment(id),
    event_key VARCHAR(128) NOT NULL UNIQUE,
    event_type VARCHAR(80) NOT NULL,
    link_status VARCHAR(40),
    amount_paid NUMERIC(14, 2),
    payment_reference VARCHAR(160),
    event_time TIMESTAMP,
    payload_snapshot VARCHAR(4000),
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS investor_agreement (
    id BIGSERIAL PRIMARY KEY,
    investor_account_id BIGINT NOT NULL REFERENCES investor_account(id),
    investment_id BIGINT NOT NULL UNIQUE REFERENCES investment(id),
    investor_payment_id BIGINT NOT NULL UNIQUE REFERENCES investor_payment(id),
    agreement_number VARCHAR(80) NOT NULL UNIQUE,
    terms_version VARCHAR(80) NOT NULL,
    terms_text TEXT NOT NULL,
    company_legal_name VARCHAR(240) NOT NULL,
    company_address VARCHAR(1000) NOT NULL,
    authorized_signatory VARCHAR(160) NOT NULL,
    investor_name VARCHAR(160) NOT NULL,
    investor_address VARCHAR(1000) NOT NULL,
    pan_masked VARCHAR(20) NOT NULL,
    principal_amount NUMERIC(14, 2) NOT NULL,
    monthly_return_rate NUMERIC(5, 2) NOT NULL,
    investment_start_date DATE NOT NULL,
    investment_end_date DATE,
    payment_reference VARCHAR(160),
    status VARCHAR(30) NOT NULL,
    generation_error VARCHAR(600),
    generated_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_investor_payment_status ON investor_payment(status);
CREATE INDEX IF NOT EXISTS idx_investor_payment_link ON investor_payment(merchant_link_id);
CREATE INDEX IF NOT EXISTS idx_investor_agreement_account ON investor_agreement(investor_account_id);
