-- V24: Re-siembra de los catálogos de gamificación (logros y objetivos).
--
-- CAUSA: la migración V17 ya está registrada como aplicada en
-- flyway_schema_history (success = t), pero las tablas achievement_catalog y
-- goal_catalog quedaron VACÍAS. Ocurrió porque en algún momento la app arrancó
-- con ddl-auto=create-drop: al apagar, Hibernate borró las tablas de entidades
-- pero conservó flyway_schema_history; al rearrancar, Flyway vio V17 como ya
-- aplicada y no la re-ejecutó, y Hibernate (ddl-auto=update) recreó las tablas
-- vacías. Consecuencia: GET /api/v1/gamification/goals devolvía [].
--
-- SOLUCIÓN: reinsertar el seed de V17 de forma IDEMPOTENTE.
--   - ON CONFLICT (code) DO NOTHING => si los datos ya existen no falla ni duplica.
--   - Bloque DO con to_regclass(...) IS NOT NULL => tolerante a que la tabla
--     todavía no exista (la creará Hibernate con ddl-auto=update desde la entidad).
-- Reutiliza los mismos UUID y version=0 de V17 para no divergir del resto de la BD.

DO $$
BEGIN
    IF to_regclass('public.achievement_catalog') IS NOT NULL THEN
        INSERT INTO achievement_catalog (id, version, created_at, updated_at, code, name, description, icon_class, category, target_value, xp_reward) VALUES
            ('20000000-0000-0000-0000-000000000001', 0, NOW(), NOW(), 'first_post', 'Nueva Voz', 'Crea tu primera publicación', 'pi-pencil', 'SOCIAL', 1, 10),
            ('20000000-0000-0000-0000-000000000002', 0, NOW(), NOW(), 'ten_posts', 'Creador', 'Crea 10 publicaciones', 'pi-camera', 'SOCIAL', 10, 100),
            ('20000000-0000-0000-0000-000000000003', 0, NOW(), NOW(), 'fifty_comments', 'Social', 'Comenta en 50 publicaciones', 'pi-comments', 'SOCIAL', 50, 200),
            ('20000000-0000-0000-0000-000000000004', 0, NOW(), NOW(), 'helper_heart', 'Corazón de la Comunidad', 'Responde 10 comentarios', 'pi-heart', 'SOCIAL', 10, 100),
            ('20000000-0000-0000-0000-000000000005', 0, NOW(), NOW(), 'like_action', 'Conectado', 'Da 25 me gusta', 'pi-thumbs-up', 'SOCIAL', 25, 50),
            ('20000000-0000-0000-0000-000000000006', 0, NOW(), NOW(), 'follow_ten', 'Explorador Social', 'Sigue a 10 usuarios', 'pi-users', 'SOCIAL', 10, 150),
            ('20000000-0000-0000-0000-000000000007', 0, NOW(), NOW(), 'first_pet', 'Primer Paso', 'Registra tu primera mascota', 'pi-heart-fill', 'PET', 1, 30),
            ('20000000-0000-0000-0000-000000000008', 0, NOW(), NOW(), 'three_pets', 'Amante de los Animales', 'Registra 3 mascotas', 'pi-inbox', 'PET', 3, 100),
            ('20000000-0000-0000-0000-000000000009', 0, NOW(), NOW(), 'profile_complete', 'Perfil Completo', 'Completa los campos de tu perfil', 'pi-user-edit', 'PROFILE', 1, 30),
            ('20000000-0000-0000-0000-000000000010', 0, NOW(), NOW(), 'vet_visits', 'Cuidador Responsable', 'Completa 5 citas médicas', 'pi-syringe', 'CARE', 5, 150),
            ('20000000-0000-0000-0000-000000000011', 0, NOW(), NOW(), 'first_purchase', 'Patrocinador', 'Realiza tu primera compra', 'pi-shopping-cart', 'SHOP', 1, 100),
            ('20000000-0000-0000-0000-000000000012', 0, NOW(), NOW(), 'first_adoption', 'Héroe de las Mascotas', 'Solicita tu primera adopción', 'pi-heart', 'ADOPTION', 1, 300),
            ('20000000-0000-0000-0000-000000000013', 0, NOW(), NOW(), 'daily_login_3', 'Constancia', 'Inicia sesión 3 veces', 'pi-calendar', 'STREAK', 3, 60)
        ON CONFLICT (code) DO NOTHING;
    END IF;
END
$$;

DO $$
BEGIN
    IF to_regclass('public.goal_catalog') IS NOT NULL THEN
        INSERT INTO goal_catalog (id, version, created_at, updated_at, code, name, description, icon_class, type, source_action, target_value, xp_reward, active) VALUES
            ('21000000-0000-0000-0000-000000000001', 0, NOW(), NOW(), 'daily_post', 'Foto del Día', 'Sube una publicación hoy', 'pi-camera', 'DAILY', 'GENERATE_POST', 1, 15, TRUE),
            ('21000000-0000-0000-0000-000000000002', 0, NOW(), NOW(), 'daily_comment', 'Comentarista', 'Comenta 3 publicaciones hoy', 'pi-comments', 'DAILY', 'CREATE_COMMENT', 3, 20, TRUE),
            ('21000000-0000-0000-0000-000000000003', 0, NOW(), NOW(), 'daily_like', 'Encantador', 'Da 5 me gusta hoy', 'pi-thumbs-up', 'DAILY', 'LIKE', 5, 10, TRUE),
            ('21000000-0000-0000-0000-000000000004', 0, NOW(), NOW(), 'daily_login', 'Presente', 'Inicia sesión hoy', 'pi-calendar', 'DAILY', 'DAILY_LOGIN', 1, 10, TRUE),
            ('21000000-0000-0000-0000-000000000005', 0, NOW(), NOW(), 'weekly_follow', 'Conecta cada semana', 'Sigue a 5 usuarios esta semana', 'pi-users', 'WEEKLY', 'FOLLOW', 5, 40, TRUE),
            ('21000000-0000-0000-0000-000000000006', 0, NOW(), NOW(), 'weekly_purchase', 'Compras de la semana', 'Realiza 1 compra esta semana', 'pi-shopping-cart', 'WEEKLY', 'PRODUCT_PURCHASED', 1, 40, TRUE)
        ON CONFLICT (code) DO NOTHING;
    END IF;
END
$$;
