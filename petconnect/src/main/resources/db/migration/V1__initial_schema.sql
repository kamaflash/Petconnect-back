-- Auth domain
CREATE TABLE IF NOT EXISTS auth_users (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    refresh_token VARCHAR(500),
    refresh_token_expiry TIMESTAMP
);

CREATE INDEX idx_auth_users_email ON auth_users(email);
CREATE INDEX idx_auth_users_refresh_token ON auth_users(refresh_token);

-- Users domain
CREATE TABLE IF NOT EXISTS user_profiles (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    auth_user_id UUID NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    bio VARCHAR(500),
    avatar_url VARCHAR(255),
    date_of_birth DATE,
    city VARCHAR(100),
    country VARCHAR(100),
    profile_public BOOLEAN NOT NULL DEFAULT TRUE,
    notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_user_profiles_auth_user_id ON user_profiles(auth_user_id);

-- Pets domain
CREATE TABLE IF NOT EXISTS pets (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    owner_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    species VARCHAR(20) NOT NULL,
    breed VARCHAR(100),
    date_of_birth DATE,
    gender VARCHAR(20),
    bio VARCHAR(500),
    avatar_url VARCHAR(255),
    weight DOUBLE PRECISION,
    weight_unit VARCHAR(20),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    microchip_id VARCHAR(100),
    color VARCHAR(100)
);

CREATE INDEX idx_pets_owner_id ON pets(owner_id);
CREATE INDEX idx_pets_species ON pets(species);