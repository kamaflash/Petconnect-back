-- Likes table for generic like functionality across all content types

-- ============================================================
-- LIKES
-- ============================================================
CREATE TABLE IF NOT EXISTS likes (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    user_id UUID NOT NULL REFERENCES user_profiles(id) ON DELETE CASCADE,
    target_id UUID NOT NULL,
    target_type VARCHAR(20) NOT NULL
);

-- Índices para optimizar consultas
CREATE INDEX idx_likes_user_id ON likes(user_id);
CREATE INDEX idx_likes_target_id_type ON likes(target_id, target_type);
CREATE INDEX idx_likes_target_type ON likes(target_type);

-- Índice único para evitar likes duplicados
CREATE UNIQUE INDEX idx_likes_unique ON likes(user_id, target_id, target_type);

-- ============================================================
-- SEED LIKES (opcional - datos de ejemplo)
-- ============================================================
-- Los likes se pueden agregar dinámicamente a través de la API