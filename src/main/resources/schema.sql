-- MySQL 8.0.16+ schema. Apply this file before data.sql.

CREATE TABLE plans (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    user_limit INT NOT NULL CHECK (user_limit > 0),
    storage_limit_gb INT NOT NULL CHECK (storage_limit_gb > 0),
    reports_per_month INT NOT NULL CHECK (reports_per_month >= 0),
    active_dashboard_limit INT NOT NULL DEFAULT 0 CHECK (active_dashboard_limit >= 0),
    data_processing_limit_gb INT NOT NULL DEFAULT 0 CHECK (data_processing_limit_gb >= 0),
    support VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

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
    status VARCHAR(10) NOT NULL CHECK (status IN ('Paid', 'Due')),
    paid_on DATE NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT invoices_period_check CHECK (period_end > period_start),
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

CREATE TABLE subscriptions (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    billing_cycle VARCHAR(20) NOT NULL CHECK (billing_cycle IN ('monthly', 'annual')),
    current_period_start DATE NOT NULL,
    current_period_end DATE NOT NULL,
    amount_due DECIMAL(10, 2) NOT NULL DEFAULT 0 CHECK (amount_due >= 0),
    last_payment_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX subscriptions_customer_id_idx (customer_id),
    INDEX subscriptions_plan_id_idx (plan_id),
    CONSTRAINT subscriptions_period_check CHECK (current_period_end > current_period_start),
    CONSTRAINT subscriptions_plan_fk FOREIGN KEY (plan_id) REFERENCES plans(id)
) ENGINE=InnoDB;
