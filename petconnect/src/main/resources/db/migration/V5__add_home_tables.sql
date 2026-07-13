-- Tables for home service data

-- ============================================================
-- SUGGESTIONS (Users to follow)
-- ============================================================
CREATE TABLE IF NOT EXISTS suggestions (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    username VARCHAR(50) NOT NULL,
    avatar_url VARCHAR(255) NOT NULL,
    followers_count INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_suggestions_created_at ON suggestions(created_at DESC);

-- ============================================================
-- MARKETPLACE PROMOS
-- ============================================================
CREATE TABLE IF NOT EXISTS market_promos (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    category VARCHAR(50) NOT NULL,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(500) NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    link_url VARCHAR(255) NOT NULL,
    badge VARCHAR(50),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_market_promos_active ON market_promos(active);

-- ============================================================
-- VET SERVICES
-- ============================================================
CREATE TABLE IF NOT EXISTS vet_services (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NOT NULL,
    icon_class VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_vet_services_active ON vet_services(active);

-- ============================================================
-- ADOPTIONS (Available pets for adoption)
-- ============================================================
CREATE TABLE IF NOT EXISTS adoptions (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    pet_name VARCHAR(100) NOT NULL,
    species VARCHAR(50) NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE'
);

CREATE INDEX idx_adoptions_status ON adoptions(status);

-- ============================================================
-- ACHIEVEMENTS
-- ============================================================
CREATE TABLE IF NOT EXISTS achievements (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NOT NULL,
    icon_class VARCHAR(100) NOT NULL,
    current_progress INTEGER NOT NULL DEFAULT 0,
    target_progress INTEGER NOT NULL,
    unlocked BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_achievements_completed ON achievements(unlocked);

-- ============================================================
-- USER LEVELS
-- ============================================================
CREATE TABLE IF NOT EXISTS user_levels (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    level INTEGER NOT NULL,
    current_xp INTEGER NOT NULL,
    next_level_xp INTEGER NOT NULL
);
