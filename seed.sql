-- Example plan values; replace prices and limits with product-approved values.
INSERT INTO plans (name, price, user_limit, storage_limit_gb, reports_per_month, support)
VALUES
    ('Basic', 9.99, 5, 10, 100, 'Email'),
    ('Standard', 29.99, 25, 100, 1000, 'Priority email'),
    ('Enterprise', 99.99, 100, 1000, 10000, 'Dedicated support')
ON CONFLICT (name) DO NOTHING;
