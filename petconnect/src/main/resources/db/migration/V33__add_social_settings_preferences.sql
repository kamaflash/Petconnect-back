ALTER TABLE user_profiles
    ADD COLUMN direct_message_policy VARCHAR(20) NOT NULL DEFAULT 'EVERYONE',
    ADD COLUMN show_online_status BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN notify_messages BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN feed_default VARCHAR(20) NOT NULL DEFAULT 'ALL';

ALTER TABLE user_profiles
    ADD CONSTRAINT chk_user_profiles_direct_message_policy
        CHECK (direct_message_policy IN ('EVERYONE', 'FOLLOWING', 'NOBODY')),
    ADD CONSTRAINT chk_user_profiles_feed_default
        CHECK (feed_default IN ('ALL', 'FOLLOWING'));
