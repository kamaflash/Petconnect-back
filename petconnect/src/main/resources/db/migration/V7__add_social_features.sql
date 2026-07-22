-- Social features: Events, Groups, Notifications, Follows, Comments

-- ============================================================
-- EVENTS
-- ============================================================
CREATE TABLE IF NOT EXISTS events (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    event_date TIMESTAMP NOT NULL,
    location VARCHAR(255),
    organizer_id UUID REFERENCES user_profiles(id),
    image_url VARCHAR(500),
    category VARCHAR(100),
    attendees_count INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_events_date ON events(event_date DESC);
CREATE INDEX idx_events_organizer ON events(organizer_id);

-- ============================================================
-- EVENT ATTENDEES
-- ============================================================
CREATE TABLE IF NOT EXISTS event_attendees (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    event_id UUID NOT NULL REFERENCES events(id),
    user_id UUID NOT NULL REFERENCES user_profiles(id),
    UNIQUE(event_id, user_id)
);

CREATE INDEX idx_event_attendees_event ON event_attendees(event_id);
CREATE INDEX idx_event_attendees_user ON event_attendees(user_id);

-- ============================================================
-- GROUPS
-- ============================================================
CREATE TABLE IF NOT EXISTS community_groups (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    avatar_url VARCHAR(500),
    category VARCHAR(100),
    members_count INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_groups_category ON community_groups(category);

-- ============================================================
-- GROUP MEMBERS
-- ============================================================
CREATE TABLE IF NOT EXISTS group_members (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    group_id UUID NOT NULL REFERENCES community_groups(id),
    user_id UUID NOT NULL REFERENCES user_profiles(id),
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    UNIQUE(group_id, user_id)
);

CREATE INDEX idx_group_members_group ON group_members(group_id);
CREATE INDEX idx_group_members_user ON group_members(user_id);

-- ============================================================
-- FOLLOWS
-- ============================================================
CREATE TABLE IF NOT EXISTS follows (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    follower_id UUID NOT NULL REFERENCES user_profiles(id),
    following_id UUID NOT NULL REFERENCES user_profiles(id),
    UNIQUE(follower_id, following_id)
);

CREATE INDEX idx_follows_follower ON follows(follower_id);
CREATE INDEX idx_follows_following ON follows(following_id);

-- ============================================================
-- NOTIFICATIONS
-- ============================================================
CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    user_id UUID NOT NULL REFERENCES user_profiles(id),
    type VARCHAR(50) NOT NULL,
    content VARCHAR(500) NOT NULL,
    read BOOLEAN NOT NULL DEFAULT FALSE,
    reference_id UUID,
    reference_type VARCHAR(50),
    actor_id UUID REFERENCES user_profiles(id)
);

CREATE INDEX idx_notifications_user ON notifications(user_id, read);
CREATE INDEX idx_notifications_created ON notifications(created_at DESC);

-- ============================================================
-- TRENDING TOPICS
-- ============================================================
CREATE TABLE IF NOT EXISTS trending_topics (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    name VARCHAR(100) NOT NULL UNIQUE,
    posts_count INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_trending_active ON trending_topics(active, posts_count DESC);

-- ============================================================
-- COMMENTS
-- ============================================================
CREATE TABLE IF NOT EXISTS comments (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    user_id UUID NOT NULL REFERENCES auth_users(id),
    target_id UUID NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    content VARCHAR(500) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_comments_target ON comments(target_id, target_type, active);
CREATE INDEX idx_comments_user ON comments(user_id, active, created_at);

-- ============================================================
-- SEED DATA

-- Events
INSERT INTO events (id, version, created_at, updated_at, title, description, event_date, location, organizer_id, image_url, category, attendees_count, active)
VALUES
    ('f0000000-0000-0000-0000-000000000001', 0, NOW(), NOW(), 'Feria de Adopciones', 'Encuentra a tu nuevo mejor amigo en esta feria de adopciones responsables.', NOW() + INTERVAL '7 days', 'Parque Central', 'b0000000-0000-0000-0000-000000000002', 'https://images.unsplash.com/photo-1548199973-03cce0bbc87b?w=600&h=300&fit=crop', 'Adopción', 156, TRUE),
    ('f0000000-0000-0000-0000-000000000002', 0, NOW(), NOW(), 'Charla: Cuidado Dental', 'Aprende sobre la importancia del cuidado dental en perros y gatos.', NOW() + INTERVAL '14 days', 'Centro Veterinario', 'b0000000-0000-0000-0000-000000000006', 'https://images.unsplash.com/photo-1583511655857-1c1bb0b7dc2e?w=600&h=300&fit=crop', 'Educación', 89, TRUE);

-- Groups
INSERT INTO community_groups (id, version, created_at, updated_at, name, description, avatar_url, category, members_count, active)
VALUES
    ('a0000000-0000-0000-0000-000000000001', 0, NOW(), NOW(), 'Amigos de los Perros', 'Comunidad para amantes de los perros de todas las razas.', 'https://i.pravatar.cc/150?img=8', 'Perros', 1234, TRUE),
    ('a0000000-0000-0000-0000-000000000002', 0, NOW(), NOW(), 'Gatos y Más', 'Todo sobre el fascinante mundo de los gatos.', 'https://i.pravatar.cc/150?img=9', 'Gatos', 876, TRUE),
    ('a0000000-0000-0000-0000-000000000003', 0, NOW(), NOW(), 'Mascotas Exóticas', 'Comparte experiencias con mascotas no convencionales.', 'https://i.pravatar.cc/150?img=10', 'Exóticos', 345, TRUE);

-- Trending topics
INSERT INTO trending_topics (id, version, created_at, updated_at, name, posts_count, active)
VALUES
    ('10000000-0000-0000-0000-000000000001', 0, NOW(), NOW(), 'Adopciones responsables', 234, TRUE),
    ('10000000-0000-0000-0000-000000000002', 0, NOW(), NOW(), 'Nutrición canina', 189, TRUE),
    ('10000000-0000-0000-0000-000000000003', 0, NOW(), NOW(), 'Comportamiento felino', 156, TRUE),
    ('10000000-0000-0000-0000-000000000004', 0, NOW(), NOW(), 'Cuidados de invierno', 123, TRUE),
    ('10000000-0000-0000-0000-000000000005', 0, NOW(), NOW(), 'Mascotas y niños', 98, TRUE);
