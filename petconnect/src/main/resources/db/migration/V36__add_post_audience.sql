ALTER TABLE posts
    ADD COLUMN audience VARCHAR(20) NOT NULL DEFAULT 'PUBLIC';

ALTER TABLE posts
    ADD CONSTRAINT chk_posts_audience
        CHECK (audience IN ('PUBLIC', 'FOLLOWERS', 'ONLY_ME'));

CREATE INDEX idx_posts_audience_active_created
    ON posts(audience, active, created_at DESC);
