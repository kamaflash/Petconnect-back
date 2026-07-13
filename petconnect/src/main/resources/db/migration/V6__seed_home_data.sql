-- Seed data for home service tables

-- ============================================================
-- SUGGESTIONS
-- ============================================================
INSERT INTO suggestions (id, version, created_at, updated_at, username, avatar_url, followers_count)
VALUES
    ('f0000000-0000-0000-0000-000000000001', 0, '2024-07-01 10:00:00', '2024-07-01 10:00:00', 'laura_vet', 'https://i.pravatar.cc/150?img=8', 1200),
    ('f0000000-0000-0000-0000-000000000002', 0, '2024-07-02 11:00:00', '2024-07-02 11:00:00', 'carlos_pets', 'https://i.pravatar.cc/150?img=9', 856),
    ('f0000000-0000-0000-0000-000000000003', 0, '2024-07-03 12:00:00', '2024-07-03 12:00:00', 'ana_adopta', 'https://i.pravatar.cc/150?img=10', 2300),
    ('f0000000-0000-0000-0000-000000000004', 0, '2024-07-04 13:00:00', '2024-07-04 13:00:00', 'pedro_mascota', 'https://i.pravatar.cc/150?img=11', 567);

-- ============================================================
-- MARKETPLACE PROMOS
-- ============================================================
INSERT INTO market_promos (id, version, created_at, updated_at, category, title, description, image_url, link_url, badge, active)
VALUES
    ('a0000000-0000-0000-0000-000000000001', 0, '2024-07-01 10:00:00', '2024-07-01 10:00:00', 'marketplace', 'Alimento Premium',
     '30% descuento en alimentos premium para perros',
     'https://images.unsplash.com/photo-1589924691195-41432c84c161?w=150&h=150&fit=crop',
     '/marketplace', 'Oferta', TRUE),
    ('a0000000-0000-0000-0000-000000000002', 0, '2024-07-02 11:00:00', '2024-07-02 11:00:00', 'marketplace', 'Juguete Interactivo',
     'Nuevo juguete que mantendra a tu mascota entretenida',
     'https://images.unsplash.com/photo-1537151608828-ea2b11777ee1?w=150&h=150&fit=crop',
     '/marketplace', 'Nuevo', TRUE);

-- ============================================================
-- VET SERVICES
-- ============================================================
INSERT INTO vet_services (id, version, created_at, updated_at, name, description, icon_class, active)
VALUES
    ('b0000000-0000-0000-0000-000000000001', 0, '2024-07-01 10:00:00', '2024-07-01 10:00:00', 'Consulta General', 'Revision completa de salud', 'pi pi-user-md', TRUE),
    ('b0000000-0000-0000-0000-000000000002', 0, '2024-07-02 11:00:00', '2024-07-02 11:00:00', 'Vacunacion', 'Esquema de vacunacion completo', 'pi pi-syringe', TRUE),
    ('b0000000-0000-0000-0000-000000000003', 0, '2024-07-03 12:00:00', '2024-07-03 12:00:00', 'Peluqueria', 'Bano y corte de pelo', 'pi pi-scissors', TRUE);

-- ============================================================
-- ADOPTIONS
-- ============================================================
INSERT INTO adoptions (id, version, created_at, updated_at, pet_name, species, image_url, status)
VALUES
    ('c0000000-0000-0000-0000-000000000001', 0, '2024-07-01 10:00:00', '2024-07-01 10:00:00', 'Rocky', 'Labrador Retriever',
     'https://images.unsplash.com/photo-1587300003388-59608b8a3669?w=150&h=150&fit=crop',
     'AVAILABLE'),
    ('c0000000-0000-0000-0000-000000000002', 0, '2024-07-02 11:00:00', '2024-07-02 11:00:00', 'Luna', 'Gato Persa',
     'https://images.unsplash.com/photo-1573865526739-59b3a0c4a4c4?w=150&h=150&fit=crop',
     'AVAILABLE');

-- ============================================================
-- ACHIEVEMENTS
-- ============================================================
INSERT INTO achievements (id, version, created_at, updated_at, name, description, icon_class, current_progress, target_progress, unlocked)
VALUES
    ('d0000000-0000-0000-0000-000000000001', 0, '2024-07-01 10:00:00', '2024-07-01 10:00:00', 'Explorador Social', 'Sigue a 10 usuarios', 'pi pi-users', 7, 10, FALSE),
    ('d0000000-0000-0000-0000-000000000002', 0, '2024-07-02 11:00:00', '2024-07-02 11:00:00', 'Amigo de los Animales', 'Comenta en 50 publicaciones', 'pi pi-comment', 32, 50, FALSE),
    ('d0000000-0000-0000-0000-000000000003', 0, '2024-07-03 12:00:00', '2024-07-03 12:00:00', 'Coleccionista', 'Guarda 25 publicaciones', 'pi pi-bookmark', 25, 25, TRUE);

-- ============================================================
-- USER LEVELS
-- ============================================================
INSERT INTO user_levels (id, version, created_at, updated_at, level, current_xp, next_level_xp)
VALUES
    ('e0000000-0000-0000-0000-000000000001', 0, '2024-07-01 10:00:00', '2024-07-01 10:00:00', 12, 650, 1000);