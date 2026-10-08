-- Apply once to an existing database created before plan activation was added.
ALTER TABLE plans
    ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;
