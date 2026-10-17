-- Propiedad de los círculos de comunidad.
--
-- created_by guarda el authUserId del usuario que creó el círculo. Es la única
-- base para decidir quién puede editar o eliminar: no se modela una tabla de
-- roles porque hoy basta con saber quién lo creó. El campo topic/moderator
-- reservado en la entidad sigue sin usarse para moderación.
--
-- Los círculos existentes quedan con created_by NULL. Siguen siendo visibles y
-- sus miembros no se ven afectados, pero no podrán ser editados ni eliminados
-- hasta que se les asigne propietario: es preferible no inventar un propietario
-- a dejar el círculo en manos de cualquiera.
ALTER TABLE community_groups ADD COLUMN IF NOT EXISTS created_by UUID;

CREATE INDEX IF NOT EXISTS idx_community_groups_created_by
    ON community_groups (created_by);
