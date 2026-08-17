-- ============================================================
-- ADOPTION REQUESTS (Solicitudes de adopción)
-- ============================================================
CREATE TABLE IF NOT EXISTS adoption_requests (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    pet_id UUID NOT NULL,
    pet_name VARCHAR(100) NOT NULL,
    pet_image VARCHAR(500),
    species VARCHAR(50),
    breed VARCHAR(100),
    owner_id UUID NOT NULL,
    adopter_id UUID NOT NULL,
    adopter_name VARCHAR(120) NOT NULL,
    message VARCHAR(1000),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
);

CREATE INDEX idx_adoption_requests_owner ON adoption_requests(owner_id);
CREATE INDEX idx_adoption_requests_adopter ON adoption_requests(adopter_id);
CREATE INDEX idx_adoption_requests_pet ON adoption_requests(pet_id);
CREATE INDEX idx_adoption_requests_status ON adoption_requests(status);
