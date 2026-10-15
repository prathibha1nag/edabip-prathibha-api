INSERT IGNORE INTO plans
    (name, price, user_limit, storage_limit_gb, reports_per_month, active_dashboard_limit, data_processing_limit_gb, support)
VALUES
    ('Basic', 9.99, 5, 10, 100, 3, 20, 'Email'),
    ('Standard', 29.99, 25, 100, 1000, 15, 200, 'Priority email'),
    ('Enterprise', 99.99, 100, 1000, 10000, 100, 2000, 'Dedicated support');

-- Example customer data for Task 2 billing endpoints; usage requires a current subscription for customer 1.
INSERT INTO customer_usage
    (customer_id, active_users, storage_used_gb, reports_generated, active_dashboards, data_processing_gb)
VALUES
    (1, 4, 6.25, 37, 2, 8.50),(2, 10, 50.00, 500, 10, 100.00),(3, 75, 750.00, 7500, 75, 1500.00)
ON DUPLICATE KEY UPDATE active_users = VALUES(active_users), storage_used_gb = VALUES(storage_used_gb),
    reports_generated = VALUES(reports_generated), active_dashboards = VALUES(active_dashboards),
    data_processing_gb = VALUES(data_processing_gb);

INSERT INTO subscriptions (customer_id, plan_id, billing_cycle, current_period_start, current_period_end, amount_due)
SELECT 1, p.id, 'monthly', DATE_SUB(CURRENT_DATE, INTERVAL DAYOFMONTH(CURRENT_DATE) - 1 DAY),
       DATE_ADD(DATE_SUB(CURRENT_DATE, INTERVAL DAYOFMONTH(CURRENT_DATE) - 1 DAY), INTERVAL 1 MONTH), p.price
FROM plans p
WHERE p.name = 'Basic'
  AND NOT EXISTS (SELECT 1 FROM subscriptions s WHERE s.customer_id = 1
                  AND s.current_period_start <= CURRENT_DATE AND s.current_period_end > CURRENT_DATE);

INSERT IGNORE INTO invoices (customer_id, period_start, period_end, amount, status, paid_on) VALUES
    (1, '2026-05-01', '2026-06-01', 9.99, 'Paid', '2026-05-01'),
    (1, '2026-06-01', '2026-07-01', 9.99, 'Paid', '2026-06-01'),
    (1, '2026-07-01', '2026-08-01', 9.99, 'Paid', '2026-07-01'),
    (1, '2026-08-01', '2026-09-01', 9.99, 'Paid', '2026-08-01'),
    (1, '2026-09-01', '2026-10-01', 9.99, 'Paid', '2026-09-01'),
    (1, '2026-10-01', '2026-11-01', 9.99, 'Due', NULL);

INSERT IGNORE INTO payment_methods (customer_id, brand, last_four, expiry_month, expiry_year, is_primary) VALUES
    (1, 'Visa', '4242', 12, 2028, TRUE),
    (1, 'Mastercard', '4444', 6, 2029, FALSE);
