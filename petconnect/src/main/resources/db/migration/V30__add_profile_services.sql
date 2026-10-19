-- V30: Servicios que ofrece un veterinario. El vet selecciona de un catálogo
-- fijo (el catálogo vive en el frontend) y se guardan los códigos elegidos
-- separados por comas, p. ej. "consultations,vaccinations,surgery".
-- Mismo patrón tolerante que V22: si la tabla no existe (historial Flyway
-- corrupto tras un create-drop), Hibernate la creará completa con la entidad.
DO $$
BEGIN
    IF to_regclass('public.user_profiles') IS NOT NULL THEN
        ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS services VARCHAR(500);
    END IF;
END
$$;
