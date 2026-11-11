ALTER TABLE auth_users
    ADD COLUMN pending_email VARCHAR(100),
    ADD COLUMN email_change_token_hash VARCHAR(64),
    ADD COLUMN email_change_token_expiry TIMESTAMP;

CREATE INDEX idx_auth_users_email_change_token_hash
    ON auth_users(email_change_token_hash);
