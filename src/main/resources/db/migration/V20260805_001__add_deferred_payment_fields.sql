ALTER TABLE purchase_order
    ADD COLUMN IF NOT EXISTS payment_method VARCHAR(40) NOT NULL DEFAULT 'ONLINE',
    ADD COLUMN IF NOT EXISTS payment_due_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS payment_collected_by VARCHAR(120),
    ADD COLUMN IF NOT EXISTS payment_collection_reference VARCHAR(140);

ALTER TABLE purchase_order
    ADD COLUMN IF NOT EXISTS cancellation_status VARCHAR(20) NOT NULL DEFAULT 'NONE',
    ADD COLUMN IF NOT EXISTS cancellation_reason VARCHAR(600),
    ADD COLUMN IF NOT EXISTS cancellation_requested_by VARCHAR(120),
    ADD COLUMN IF NOT EXISTS cancellation_requested_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS cancellation_decision_note VARCHAR(600);

ALTER TABLE order_refund
    ADD COLUMN IF NOT EXISTS refund_method VARCHAR(30) NOT NULL DEFAULT 'CASHFREE';

ALTER TABLE customer
    ADD COLUMN IF NOT EXISTS deferred_payment_eligible BOOLEAN NOT NULL DEFAULT FALSE;
