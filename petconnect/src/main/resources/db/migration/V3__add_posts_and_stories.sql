-- Posts and Stories tables for the social feed

-- ============================================================
-- POSTS
-- ============================================================
CREATE TABLE IF NOT EXISTS posts (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    author_id UUID NOT NULL REFERENCES user_profiles(id),
    image_url VARCHAR(500) NOT NULL,
    caption VARCHAR(1000),
    likes_count INTEGER NOT NULL DEFAULT 0,
    comments_count INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_posts_author_id ON posts(author_id);
CREATE INDEX idx_posts_created_at ON posts(created_at DESC);

-- ============================================================
-- STORIES
-- ============================================================
CREATE TABLE IF NOT EXISTS stories (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    user_id UUID NOT NULL REFERENCES user_profiles(id),
    image_url VARCHAR(500),
    seen BOOLEAN NOT NULL DEFAULT FALSE,
    expires_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_stories_user_id ON stories(user_id);
CREATE INDEX idx_stories_expires_at ON stories(expires_at);

-- ============================================================
-- SEED POSTS
-- ============================================================
INSERT INTO posts (id, version, created_at, updated_at, author_id, image_url, caption, likes_count, comments_count, active)
VALUES
    -- Post de Juan Pérez (b0000000-...-002)
    ('d0000000-0000-0000-0000-000000000001', 0, '2024-06-01 10:30:00', '2024-06-01 10:30:00',
     'b0000000-0000-0000-0000-000000000002',
     'https://images.unsplash.com/photo-1587300003388-59608b8a3669?w=600&h=600&fit=crop',
     '¡Max encontró un nuevo amigo en el parque! 🐕 Los perros son los mejores compañeros de aventuras. #PerrosFelices #ParqueCentral',
     1247, 89, TRUE),

    ('d0000000-0000-0000-0000-000000000002', 0, '2024-06-05 14:00:00', '2024-06-05 14:00:00',
     'b0000000-0000-0000-0000-000000000002',
     'https://images.unsplash.com/photo-1548199973-03cce0bbc87b?w=600&h=600&fit=crop',
     'Luna disfrutando de su siesta favorita al sol 🌞 Los gatos saben vivir la vida. #GatosFelices #Siesta',
     893, 67, TRUE),

    -- Post del Refugio Esperanza (b0000000-...-010)
    ('d0000000-0000-0000-0000-000000000003', 0, '2024-06-10 09:00:00', '2024-06-10 09:00:00',
     'b0000000-0000-0000-0000-000000000010',
     'https://images.unsplash.com/photo-1583511655857-1c1bb0b7dc2e?w=600&h=600&fit=crop',
     '¡Luna encontró una familia increíble! 🏠❤️ Hoy es un día de celebración. #Adopción #HistoriasFelices',
     2156, 142, TRUE),

    ('d0000000-0000-0000-0000-000000000004', 0, '2024-06-15 11:00:00', '2024-06-15 11:00:00',
     'b0000000-0000-0000-0000-000000000010',
     'https://images.unsplash.com/photo-1601758228041-f3b2795255f1?w=600&h=600&fit=crop',
     'Chispa ya está listo para ser adoptado. Es un perro cariñoso y juguetón que busca un hogar. 🐕 #AdoptaNoCompres',
     342, 23, TRUE),

    -- Post de PetShop Express (b0000000-...-012)
    ('d0000000-0000-0000-0000-000000000005', 0, '2024-06-20 16:00:00', '2024-06-20 16:00:00',
     'b0000000-0000-0000-0000-000000000012',
     'https://images.unsplash.com/photo-1589924691195-41432c84c161?w=600&h=600&fit=crop',
     'Nuevo lanzamiento de snacks saludables 🍖 100% naturales y orgánicos. ¡Tus mascotas lo van a amar! #PetShop #SnacksSaludables',
     567, 34, TRUE),

    -- Post de Ana García (b0000000-...-003)
    ('d0000000-0000-0000-0000-000000000006', 0, '2024-06-25 08:00:00', '2024-06-25 08:00:00',
     'b0000000-0000-0000-0000-000000000003',
     'https://images.unsplash.com/photo-1573865526739-59b3a0c4a4c4?w=600&h=600&fit=crop',
     'Misi y Garfield después de su visita al veterinario. Todo bien, solo revisión de rutina. 🐱 #GatosRescatados #Veterinaria',
     723, 45, TRUE),

    -- Post de Carlos López (b0000000-...-004)
    ('d0000000-0000-0000-0000-000000000007', 0, '2024-07-01 07:30:00', '2024-07-01 07:30:00',
     'b0000000-0000-0000-0000-000000000004',
     'https://images.unsplash.com/photo-1553882809-a4f57e595701?w=600&h=600&fit=crop',
     'Thor y Daisy después de su sesión de entrenamiento. Cada día mejoran más. 🐕‍🦺 #AdiestramientoCanino #PastorAleman #BorderCollie',
     1024, 78, TRUE),

    -- Post de Pedro Sánchez (b0000000-...-006)
    ('d0000000-0000-0000-0000-000000000008', 0, '2024-07-05 12:00:00', '2024-07-05 12:00:00',
     'b0000000-0000-0000-0000-000000000006',
     'https://images.unsplash.com/photo-1552053831-71594a27632d?w=600&h=600&fit=crop',
     'Ruta de senderismo con Nieve por los Pirineos. Ella es la mejor compañera de aventuras. 🏔️🐕 #Husky #Senderismo #Montaña',
     1567, 92, TRUE);

-- ============================================================
-- SEED STORIES
-- ============================================================
INSERT INTO stories (id, version, created_at, updated_at, user_id, image_url, seen, expires_at)
VALUES
    ('e0000000-0000-0000-0000-000000000001', 0, '2024-07-10 08:00:00', '2024-07-10 08:00:00',
     'b0000000-0000-0000-0000-000000000002', NULL, FALSE, '2024-07-11 08:00:00'),
    ('e0000000-0000-0000-0000-000000000002', 0, '2024-07-10 09:00:00', '2024-07-10 09:00:00',
     'b0000000-0000-0000-0000-000000000003', NULL, FALSE, '2024-07-11 09:00:00'),
    ('e0000000-0000-0000-0000-000000000003', 0, '2024-07-10 10:00:00', '2024-07-10 10:00:00',
     'b0000000-0000-0000-0000-000000000004', NULL, TRUE, '2024-07-11 10:00:00'),
    ('e0000000-0000-0000-0000-000000000004', 0, '2024-07-10 11:00:00', '2024-07-10 11:00:00',
     'b0000000-0000-0000-0000-000000000005', NULL, FALSE, '2024-07-11 11:00:00'),
    ('e0000000-0000-0000-0000-000000000005', 0, '2024-07-10 12:00:00', '2024-07-10 12:00:00',
     'b0000000-0000-0000-0000-000000000006', NULL, FALSE, '2024-07-11 12:00:00'),
    ('e0000000-0000-0000-0000-000000000006', 0, '2024-07-10 13:00:00', '2024-07-10 13:00:00',
     'b0000000-0000-0000-0000-000000000010', NULL, TRUE, '2024-07-11 13:00:00');