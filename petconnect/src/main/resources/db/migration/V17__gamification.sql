-- ============================================================
-- Módulo de Gamificación
-- ============================================================

-- ============================================================
-- USER GAMIFICATION (estado por usuario: nivel 0 inicial, XP, racha)
-- ============================================================
CREATE TABLE IF NOT EXISTS user_gamification (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    user_id UUID NOT NULL UNIQUE,
    level INTEGER NOT NULL DEFAULT 0,
    total_xp INTEGER NOT NULL DEFAULT 0,
    current_level_xp INTEGER NOT NULL DEFAULT 0,
    xp_to_level INTEGER NOT NULL DEFAULT 100,
    rank_title VARCHAR(40) NOT NULL DEFAULT 'Novato/a',
    daily_xp_earned INTEGER NOT NULL DEFAULT 0,
    daily_xp_date DATE,
    streak_days INTEGER NOT NULL DEFAULT 0,
    last_login_date DATE
);

CREATE INDEX idx_user_gamification_user_id ON user_gamification(user_id);

-- ============================================================
-- ACHIEVEMENT CATALOG (logros fijos) + USER ACHIEVEMENTS (progreso)
-- ============================================================
CREATE TABLE IF NOT EXISTS achievement_catalog (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NOT NULL,
    icon_class VARCHAR(100) NOT NULL,
    category VARCHAR(20) NOT NULL,
    target_value INTEGER NOT NULL,
    xp_reward INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS user_achievements (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    user_id UUID NOT NULL,
    achievement_code VARCHAR(50) NOT NULL,
    progress INTEGER NOT NULL DEFAULT 0,
    state VARCHAR(20) NOT NULL DEFAULT 'LOCKED',
    unlocked_at TIMESTAMP,
    UNIQUE (user_id, achievement_code)
);

CREATE INDEX idx_user_achievements_user_id ON user_achievements(user_id);

-- ============================================================
-- GOAL CATALOG (objetivos variables) + USER GOAL PROGRESS
-- ============================================================
CREATE TABLE IF NOT EXISTS goal_catalog (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NOT NULL,
    icon_class VARCHAR(100) NOT NULL,
    type VARCHAR(10) NOT NULL,
    source_action VARCHAR(40) NOT NULL,
    target_value INTEGER NOT NULL,
    xp_reward INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS user_goal_progress (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    user_id UUID NOT NULL,
    goal_code VARCHAR(50) NOT NULL,
    type VARCHAR(10) NOT NULL,
    progress INTEGER NOT NULL DEFAULT 0,
    period_start TIMESTAMP NOT NULL,
    period_end TIMESTAMP,
    claimed BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (user_id, goal_code)
);

CREATE INDEX idx_user_goal_progress_user_id ON user_goal_progress(user_id);

-- ============================================================
-- XP TRANSACTIONS (auditoría / anti-grind)
-- ============================================================
CREATE TABLE IF NOT EXISTS xp_transactions (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    user_id UUID NOT NULL,
    action_type VARCHAR(40) NOT NULL,
    amount INTEGER NOT NULL,
    xp_on DATE NOT NULL
);

CREATE INDEX idx_xp_transactions_user_on ON xp_transactions(user_id, xp_on);
-- ============================================================
-- SEED: catálogo de logros fijos
-- ============================================================
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
    ('20000000-0000-0000-0000-000000000013', 0, NOW(), NOW(), 'daily_login_3', 'Constancia', 'Inicia sesión 3 veces', 'pi-calendar', 'STREAK', 3, 60);

-- ============================================================
-- SEED: catálogo de objetivos variables (diarios/semanales)
-- ============================================================
INSERT INTO goal_catalog (id, version, created_at, updated_at, code, name, description, icon_class, type, source_action, target_value, xp_reward, active) VALUES
    ('21000000-0000-0000-0000-000000000001', 0, NOW(), NOW(), 'daily_post', 'Foto del Día', 'Sube una publicación hoy', 'pi-camera', 'DAILY', 'GENERATE_POST', 1, 15, TRUE),
    ('21000000-0000-0000-0000-000000000002', 0, NOW(), NOW(), 'daily_comment', 'Comentarista', 'Comenta 3 publicaciones hoy', 'pi-comments', 'DAILY', 'CREATE_COMMENT', 3, 20, TRUE),
    ('21000000-0000-0000-0000-000000000003', 0, NOW(), NOW(), 'daily_like', 'Encantador', 'Da 5 me gusta hoy', 'pi-thumbs-up', 'DAILY', 'LIKE', 5, 10, TRUE),
    ('21000000-0000-0000-0000-000000000004', 0, NOW(), NOW(), 'daily_login', 'Presente', 'Inicia sesión hoy', 'pi-calendar', 'DAILY', 'DAILY_LOGIN', 1, 10, TRUE),
    ('21000000-0000-0000-0000-000000000005', 0, NOW(), NOW(), 'weekly_follow', 'Conecta cada semana', 'Sigue a 5 usuarios esta semana', 'pi-users', 'WEEKLY', 'FOLLOW', 5, 40, TRUE),
    ('21000000-0000-0000-0000-000000000006', 0, NOW(), NOW(), 'weekly_purchase', 'Compras de la semana', 'Realiza 1 compra esta semana', 'pi-shopping-cart', 'WEEKLY', 'PRODUCT_PURCHASED', 1, 40, TRUE);