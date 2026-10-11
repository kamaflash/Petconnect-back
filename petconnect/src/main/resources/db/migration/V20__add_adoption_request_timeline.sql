CREATE TABLE IF NOT EXISTS adoption_request_events (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    request_id UUID NOT NULL REFERENCES adoption_requests(id) ON DELETE CASCADE,
    actor_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    note VARCHAR(500)
);

CREATE INDEX IF NOT EXISTS idx_adoption_request_events_request_date
    ON adoption_request_events(request_id, created_at);

INSERT INTO adoption_request_events (id, version, created_at, updated_at, request_id, actor_id, status, note)
SELECT gen_random_uuid(), 0, r.updated_at, r.updated_at, r.id, r.adopter_id, r.status, NULL
FROM adoption_requests r
WHERE NOT EXISTS (
    SELECT 1 FROM adoption_request_events e WHERE e.request_id = r.id
);
