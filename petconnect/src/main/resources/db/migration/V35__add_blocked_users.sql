CREATE TABLE blocked_users (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    blocker_auth_user_id UUID NOT NULL REFERENCES auth_users(id) ON DELETE CASCADE,
    blocked_auth_user_id UUID NOT NULL REFERENCES auth_users(id) ON DELETE CASCADE,
    CONSTRAINT chk_blocked_users_not_self CHECK (blocker_auth_user_id <> blocked_auth_user_id),
    CONSTRAINT uk_blocked_users_pair UNIQUE (blocker_auth_user_id, blocked_auth_user_id)
);

CREATE INDEX idx_blocked_users_blocker ON blocked_users(blocker_auth_user_id);
CREATE INDEX idx_blocked_users_blocked ON blocked_users(blocked_auth_user_id);
