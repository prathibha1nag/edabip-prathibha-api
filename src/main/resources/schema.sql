-- MySQL 8.0.16+ schema. Apply this file before data.sql.

CREATE TABLE plans (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    user_limit INT NOT NULL CHECK (user_limit > 0),
    storage_limit_gb INT NOT NULL CHECK (storage_limit_gb > 0),
    reports_per_month INT NOT NULL CHECK (reports_per_month >= 0),
    support VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
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
