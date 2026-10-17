-- Membresías de círculos de comunidad.
--
-- Se modela como tabla propia en lugar de un contador para poder responder
-- "a qué círculos pertenezco" y evitar que un usuario se una dos veces al
-- mismo círculo (unique constraint). El contador members_count de
-- community_groups se mantiene como dato cacheado para el listado.
CREATE TABLE IF NOT EXISTS community_group_members (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    group_id UUID NOT NULL REFERENCES community_groups(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    CONSTRAINT uq_community_member UNIQUE (group_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_community_members_group
    ON community_group_members (group_id);

CREATE INDEX IF NOT EXISTS idx_community_members_user
    ON community_group_members (user_id);

-- Temática e imagen de los círculos. Ambas nullable: los círculos creados
-- antes de esta migración siguen siendo válidos y se muestran sin temática.
ALTER TABLE community_groups ADD COLUMN IF NOT EXISTS topic VARCHAR(50);
