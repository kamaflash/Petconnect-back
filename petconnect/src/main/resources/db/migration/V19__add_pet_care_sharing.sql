CREATE TABLE IF NOT EXISTS pet_care_shares (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    pet_id UUID NOT NULL REFERENCES pets(id) ON DELETE CASCADE,
    owner_id UUID NOT NULL,
    invited_user_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT uk_pet_care_share_user UNIQUE (pet_id, invited_user_id)
);

CREATE INDEX IF NOT EXISTS idx_pet_care_shares_invited_user
    ON pet_care_shares(invited_user_id, status);

CREATE TABLE IF NOT EXISTS pet_care_activities (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    pet_id UUID NOT NULL REFERENCES pets(id) ON DELETE CASCADE,
    actor_id UUID NOT NULL,
    activity_type VARCHAR(30) NOT NULL,
    notes VARCHAR(500),
    occurred_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_pet_care_activities_pet_date
    ON pet_care_activities(pet_id, occurred_at DESC);
