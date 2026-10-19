-- V31: Marca de onboarding del perfil. Vale true cuando el usuario ha elegido
-- su tipo de cuenta y ha confirmado los datos obligatorios de ese tipo.
-- Se usa para bloquear las rutas protegidas hasta completar el onboarding.
-- Los perfiles ya existentes se marcan como completados si tienen los campos
-- obligatorios de su tipo, para no bloquear cuentas creadas antes de este
-- cambio. Los perfiles nuevos (Google / registro) nacen en false.
DO $$
BEGIN
    IF to_regclass('public.user_profiles') IS NOT NULL THEN
        ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS profile_completed BOOLEAN NOT NULL DEFAULT FALSE;

        UPDATE user_profiles
        SET profile_completed = TRUE
        WHERE first_name IS NOT NULL AND BTRIM(first_name) <> ''
          AND last_name IS NOT NULL AND BTRIM(last_name) <> ''
          AND profile_type IS NOT NULL AND BTRIM(profile_type) <> ''
          AND (
                (LOWER(BTRIM(profile_type)) = 'user')
             OR (LOWER(BTRIM(profile_type)) = 'vet'
                 AND license_number IS NOT NULL AND BTRIM(license_number) <> ''
                 AND services IS NOT NULL AND BTRIM(services) <> '')
             OR (LOWER(BTRIM(profile_type)) = 'shelter'
                 AND charity_number IS NOT NULL AND BTRIM(charity_number) <> '')
             OR (LOWER(BTRIM(profile_type)) = 'trainer'
                 AND specialty IS NOT NULL AND BTRIM(specialty) <> '')
             OR (LOWER(BTRIM(profile_type)) = 'seller'
                 AND store_name IS NOT NULL AND BTRIM(store_name) <> '')
          );
    END IF;
END
$$;