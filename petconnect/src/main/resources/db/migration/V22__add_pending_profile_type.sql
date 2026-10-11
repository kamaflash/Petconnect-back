-- V22: Cambio de tipo de perfil (vet/shelter) pendiente de aprobación de administrador.
-- El tipo activo no cambia hasta que un admin apruebe la solicitud.
-- Se incluyen además las columnas de campos por tipo (specialty, licenseNumber,
-- charityNumber, storeName, website) que antes solo creaba Hibernate con
-- ddl-auto=create-drop y no tenían migración propia.
-- El bloque DO hace la migración tolerante a una BD vacía: si la tabla no
-- existe (historial Flyway corrupto tras un create-drop), Hibernate con
-- ddl-auto=update la creará con todas las columnas de la entidad.
DO $$
BEGIN
    IF to_regclass('public.user_profiles') IS NOT NULL THEN
        ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS pending_profile_type VARCHAR(20);
        ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS specialty VARCHAR(100);
        ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS license_number VARCHAR(50);
        ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS charity_number VARCHAR(100);
        ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS store_name VARCHAR(100);
        ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS website VARCHAR(255);
    END IF;
END
$$;
