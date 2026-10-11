-- ============================================================
-- ANIMALES PERDIDOS: avisos y avistamientos
-- ============================================================
CREATE TABLE IF NOT EXISTS lost_pet_reports (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    reporter_id UUID NOT NULL,
    pet_id UUID,
    pet_name VARCHAR(100) NOT NULL,
    species VARCHAR(30) NOT NULL,
    breed VARCHAR(100),
    color VARCHAR(500),
    description VARCHAR(1000),
    photo_url VARCHAR(500),
    lost_date DATE NOT NULL,
    last_seen_area VARCHAR(255) NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    reward_amount DOUBLE PRECISION,
    status VARCHAR(20) NOT NULL DEFAULT 'LOST',
    sightings_count INTEGER NOT NULL DEFAULT 0,
    closed_at DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_lost_pet_reports_active_created
    ON lost_pet_reports(active, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_lost_pet_reports_reporter
    ON lost_pet_reports(reporter_id);
CREATE INDEX IF NOT EXISTS idx_lost_pet_reports_species_status
    ON lost_pet_reports(species, status);
CREATE INDEX IF NOT EXISTS idx_lost_pet_reports_lost_date
    ON lost_pet_reports(lost_date DESC);

CREATE TABLE IF NOT EXISTS lost_pet_sightings (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    report_id UUID NOT NULL REFERENCES lost_pet_reports(id) ON DELETE CASCADE,
    reporter_id UUID NOT NULL,
    sighted_at TIMESTAMP NOT NULL,
    description VARCHAR(1000) NOT NULL,
    area VARCHAR(255),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    photo_url VARCHAR(500)
);

CREATE INDEX IF NOT EXISTS idx_lost_pet_sightings_report
    ON lost_pet_sightings(report_id, sighted_at DESC);
CREATE INDEX IF NOT EXISTS idx_lost_pet_sightings_reporter
    ON lost_pet_sightings(reporter_id);
