-- V29: Seed de las tablas de catálogo que aún no tenían datos iniciales.
-- Todas las inserciones son IDEMPOTENTES (ON CONFLICT DO NOTHING) para que
-- se puedan ejecutar tantas veces como sea necesario sin fallar ni duplicar.
-- Las tablas transaccionales relacionales (likes, follows, comments, messages,
-- conversation_participants, event_attendees, group_members, user_achievements,
-- user_goal_progress, xp_transactions, pet_care_activities, lost_pet_reports,
-- lost_pet_sightings, pet_ownership_history, adoption_request_events,
-- community_group_members, etc.) dependen de datos de usuario/mascota y no
-- tiene sentido poblarlas estáticamente; se dejan vacías para que la aplicación
-- las rellene mediante su uso normal.

-- ============================================================
-- PRODUCTS (marketplace)
-- ============================================================
INSERT INTO products (id, seller_id, name, description, price, original_price, category, subcategory, image_url, stock, brand, rating, reviews, active, discount, tags, version, created_at, updated_at)
VALUES
    ('30000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000012', 'Alimento Premium Perro Adulto', 'Saco de 15 kg de alimento balanceado para perros adultos.', 45.90, 52.00, 'Alimento', 'Perros', 'https://images.unsplash.com/photo-1589924691195-41432c84c161?w=600&h=600&fit=crop', 100, 'PetNutri', 4.5, 120, TRUE, 10, 'perro,alimento,premium', 0, '2024-07-01 10:00:00', '2024-07-01 10:00:00'),
    ('30000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000012', 'Juguete Interactivo para Gatos', 'Juguete con plumas y campana para estimular el instinto cazador.', 12.50, NULL, 'Juguetes', 'Gatos', 'https://images.unsplash.com/photo-1537151608828-ea2b11777ee1?w=600&h=600&fit=crop', 50, 'KittyFun', 4.2, 85, TRUE, 0, 'gato,juguete,interactivo', 0, '2024-07-02 11:00:00', '2024-07-02 11:00:00')
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- ADOPTION_REQUESTS (solicitudes de adopción de ejemplo)
-- ============================================================
INSERT INTO adoption_requests (id, version, created_at, updated_at, pet_id, pet_name, pet_image, species, breed, owner_id, adopter_id, adopter_name, message, status)
VALUES
    ('40000000-0000-0000-0000-000000000001', 0, '2024-07-05 09:00:00', '2024-07-05 09:00:00',
     'c0000000-0000-0000-0000-000000000011', 'Chispa', 'https://images.unsplash.com/photo-1583511655857-1c1bb0b7dc2e?w=600&h=600&fit=crop', 'DOG', 'Mestizo',
     'b0000000-0000-0000-0000-000000000010', 'b0000000-0000-0000-0000-000000000002', 'Juan Pérez', 'Me interesa mucho adoptar a Chispa, tengo jardín y experiencia con perros.', 'PENDING'),
    ('40000000-0000-0000-0000-000000000002', 0, '2024-07-06 10:30:00', '2024-07-06 10:30:00',
     'c0000000-0000-0000-0000-000000000012', 'Canela', 'https://images.unsplash.com/photo-1518791841237-8f94e67e9e7b?w=600&h=600&fit=crop', 'CAT', 'Mestizo',
     'b0000000-0000-0000-0000-000000000010', 'b0000000-0000-0000-0000-000000000003', 'Ana García', 'Busco una gata tranquila para acompañarme en casa.', 'APPROVED')
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- PET_CARE_SHARES (compartidos de cuidado de mascota)
-- ============================================================
INSERT INTO pet_care_shares (id, version, created_at, updated_at, pet_id, owner_id, invited_user_id, role, status)
VALUES
    ('50000000-0000-0000-0000-000000000001', 0, '2024-07-01 10:00:00', '2024-07-01 10:00:00',
     'c0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000003', 'CAREGIVER', 'ACTIVE'),
    ('50000000-0000-0000-0000-000000000002', 0, '2024-07-02 11:00:00', '2024-07-02 11:00:00',
     'c0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000004', 'VIEWER', 'PENDING');