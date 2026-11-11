CREATE TABLE shelter_donation_campaigns (
    id UUID PRIMARY KEY,
    version BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    shelter_user_id UUID NOT NULL,
    title VARCHAR(120) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    donation_url VARCHAR(2048) NOT NULL,
    image_url VARCHAR(2048),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_shelter_donation_campaigns_active
    ON shelter_donation_campaigns(shelter_user_id, active, created_at DESC);

CREATE TABLE shelter_visit_requests (
    id UUID PRIMARY KEY,
    version BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    shelter_user_id UUID NOT NULL,
    visitor_user_id UUID NOT NULL,
    visitor_name VARCHAR(120) NOT NULL,
    visit_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    proposed_visit_at TIMESTAMP WITHOUT TIME ZONE,
    party_size INTEGER NOT NULL,
    message VARCHAR(1000),
    status VARCHAR(30) NOT NULL,
    response_note VARCHAR(1000),
    CONSTRAINT chk_shelter_visit_party_size CHECK (party_size BETWEEN 1 AND 20),
    CONSTRAINT chk_shelter_visit_status CHECK (
        status IN ('PENDING', 'CONFIRMED', 'ALTERNATIVE_PROPOSED', 'REJECTED', 'COMPLETED', 'CANCELLED')
    )
);

CREATE INDEX idx_shelter_visit_requests_shelter_date
    ON shelter_visit_requests(shelter_user_id, visit_at);
