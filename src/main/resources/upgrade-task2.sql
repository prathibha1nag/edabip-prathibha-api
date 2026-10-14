-- Apply once to a Task 1 database. Back up the database before applying migrations.
ALTER TABLE plans
    ADD COLUMN active_dashboard_limit INT NOT NULL DEFAULT 0 CHECK (active_dashboard_limit >= 0),
    ADD COLUMN data_processing_limit_gb INT NOT NULL DEFAULT 0 CHECK (data_processing_limit_gb >= 0);

CREATE TABLE customer_usage (
    customer_id BIGINT PRIMARY KEY,
    active_users INT NOT NULL DEFAULT 0 CHECK (active_users >= 0),
    storage_used_gb DECIMAL(12, 2) NOT NULL DEFAULT 0 CHECK (storage_used_gb >= 0),
    reports_generated INT NOT NULL DEFAULT 0 CHECK (reports_generated >= 0),
    active_dashboards INT NOT NULL DEFAULT 0 CHECK (active_dashboards >= 0),
    data_processing_gb DECIMAL(12, 2) NOT NULL DEFAULT 0 CHECK (data_processing_gb >= 0),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE invoices (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    amount DECIMAL(10, 2) NOT NULL CHECK (amount >= 0),
    status VARCHAR(10) NOT NULL,
    paid_on DATE NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT invoices_period_check CHECK (period_end > period_start),
    CONSTRAINT invoices_status_check CHECK (status IN ('Paid', 'Due')),
    CONSTRAINT invoices_paid_on_check CHECK ((status = 'Paid' AND paid_on IS NOT NULL) OR (status = 'Due' AND paid_on IS NULL)),
    INDEX invoices_customer_period_idx (customer_id, period_start),
    UNIQUE KEY invoices_customer_period_uq (customer_id, period_start)
) ENGINE=InnoDB;

CREATE TABLE payment_methods (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    brand VARCHAR(30) NOT NULL,
    last_four CHAR(4) NOT NULL,
    expiry_month TINYINT NOT NULL CHECK (expiry_month BETWEEN 1 AND 12),
    expiry_year SMALLINT NOT NULL CHECK (expiry_year >= 2000),
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    primary_customer_id BIGINT GENERATED ALWAYS AS (CASE WHEN is_primary THEN customer_id ELSE NULL END) STORED,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX payment_methods_customer_idx (customer_id),
    UNIQUE KEY payment_methods_identity_uq (customer_id, brand, last_four, expiry_month, expiry_year),
    UNIQUE KEY payment_methods_one_primary_per_customer_uq (primary_customer_id)
) ENGINE=InnoDB;

-- Use explicit, product-approved limits when configuring plans. These are sample values.
UPDATE plans SET active_dashboard_limit = CASE name
    WHEN 'Basic' THEN 3 WHEN 'Standard' THEN 15 WHEN 'Enterprise' THEN 100 ELSE active_dashboard_limit END,
    data_processing_limit_gb = CASE name
    WHEN 'Basic' THEN 20 WHEN 'Standard' THEN 200 WHEN 'Enterprise' THEN 2000 ELSE data_processing_limit_gb END;

INSERT INTO customer_usage
    (customer_id, active_users, storage_used_gb, reports_generated, active_dashboards, data_processing_gb)
VALUES (1, 4, 6.25, 37, 2, 8.50);

INSERT INTO subscriptions (customer_id, plan_id, billing_cycle, current_period_start, current_period_end, amount_due)
SELECT 1, p.id, 'monthly', DATE_SUB(CURRENT_DATE, INTERVAL DAYOFMONTH(CURRENT_DATE) - 1 DAY),
       DATE_ADD(DATE_SUB(CURRENT_DATE, INTERVAL DAYOFMONTH(CURRENT_DATE) - 1 DAY), INTERVAL 1 MONTH), p.price
FROM plans p
WHERE p.name = 'Basic'
  AND NOT EXISTS (SELECT 1 FROM subscriptions s WHERE s.customer_id = 1
                  AND s.current_period_start <= CURRENT_DATE AND s.current_period_end > CURRENT_DATE);

INSERT INTO invoices (customer_id, period_start, period_end, amount, status, paid_on) VALUES
    (1, '2026-05-01', '2026-06-01', 9.99, 'Paid', '2026-05-01'),
    (1, '2026-06-01', '2026-07-01', 9.99, 'Paid', '2026-06-01'),
    (1, '2026-07-01', '2026-08-01', 9.99, 'Paid', '2026-07-01'),
    (1, '2026-08-01', '2026-09-01', 9.99, 'Paid', '2026-08-01'),
    (1, '2026-09-01', '2026-10-01', 9.99, 'Paid', '2026-09-01'),
    (1, '2026-10-01', '2026-11-01', 9.99, 'Due', NULL);

INSERT INTO payment_methods (customer_id, brand, last_four, expiry_month, expiry_year, is_primary) VALUES
    (1, 'Visa', '4242', 12, 2028, TRUE),
    (1, 'Mastercard', '4444', 6, 2029, FALSE);
