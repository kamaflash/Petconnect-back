-- Seed data for development and testing
-- All passwords are 'Petconnect1!' (BCrypt hashed)
-- Password hash: $2b$10$FgO/DYypFxQDRSq7.9JEJOJwVNKUKxCPp.aQlbt42c56/RrAHt0ri

-- ============================================================
-- AUTH USERS
-- ============================================================
INSERT INTO auth_users (id, version, created_at, updated_at, email, password, role, enabled, email_verified)
VALUES
    -- Admin
    ('a0000000-0000-0000-0000-000000000001', 0, '2024-01-01 10:00:00', '2024-01-01 10:00:00',
     'admin@petconnect.com',
     '$2b$10$FgO/DYypFxQDRSq7.9JEJOJwVNKUKxCPp.aQlbt42c56/RrAHt0ri',
     'ADMIN', TRUE, TRUE),

    -- Usuario particular: Juan Pérez
    ('a0000000-0000-0000-0000-000000000002', 0, '2024-01-15 12:30:00', '2024-01-15 12:30:00',
     'juan@email.com',
     '$2b$10$FgO/DYypFxQDRSq7.9JEJOJwVNKUKxCPp.aQlbt42c56/RrAHt0ri',
     'USER', TRUE, TRUE),

    -- Usuario particular: Ana García
    ('a0000000-0000-0000-0000-000000000003', 0, '2024-02-01 09:00:00', '2024-02-01 09:00:00',
     'ana@email.com',
     '$2b$10$FgO/DYypFxQDRSq7.9JEJOJwVNKUKxCPp.aQlbt42c56/RrAHt0ri',
     'USER', TRUE, TRUE),

    -- Usuario particular: Carlos López
    ('a0000000-0000-0000-0000-000000000004', 0, '2024-02-20 16:45:00', '2024-02-20 16:45:00',
     'carlos.lopez@email.com',
     '$2b$10$FgO/DYypFxQDRSq7.9JEJOJwVNKUKxCPp.aQlbt42c56/RrAHt0ri',
     'USER', TRUE, TRUE),

    -- Usuario particular: Laura Martínez
    ('a0000000-0000-0000-0000-000000000005', 0, '2024-03-05 11:20:00', '2024-03-05 11:20:00',
     'laura@email.com',
     '$2b$10$FgO/DYypFxQDRSq7.9JEJOJwVNKUKxCPp.aQlbt42c56/RrAHt0ri',
     'USER', TRUE, TRUE),

    -- Usuario particular: Pedro Sánchez
    ('a0000000-0000-0000-0000-000000000006', 0, '2024-03-15 08:30:00', '2024-03-15 08:30:00',
     'pedro@email.com',
     '$2b$10$FgO/DYypFxQDRSq7.9JEJOJwVNKUKxCPp.aQlbt42c56/RrAHt0ri',
     'USER', TRUE, TRUE),

    -- Refugio Esperanza (shelter)
    ('a0000000-0000-0000-0000-000000000010', 0, '2024-01-20 14:00:00', '2024-01-20 14:00:00',
     'refugio@email.com',
     '$2b$10$FgO/DYypFxQDRSq7.9JEJOJwVNKUKxCPp.aQlbt42c56/RrAHt0ri',
     'BUSINESS', TRUE, TRUE),

    -- Dr. Carlos Martínez (vet)
    ('a0000000-0000-0000-0000-000000000011', 0, '2024-02-10 10:00:00', '2024-02-10 10:00:00',
     'carlos@email.com',
     '$2b$10$FgO/DYypFxQDRSq7.9JEJOJwVNKUKxCPp.aQlbt42c56/RrAHt0ri',
     'VET', TRUE, TRUE),

    -- PetShop Express (seller)
    ('a0000000-0000-0000-0000-000000000012', 0, '2024-03-01 09:30:00', '2024-03-01 09:30:00',
     'petshop@email.com',
     '$2b$10$FgO/DYypFxQDRSq7.9JEJOJwVNKUKxCPp.aQlbt42c56/RrAHt0ri',
     'BUSINESS', TRUE, TRUE),

    -- Premium user: María Rodríguez
    ('a0000000-0000-0000-0000-000000000007', 0, '2024-04-01 13:00:00', '2024-04-01 13:00:00',
     'maria@email.com',
     '$2b$10$FgO/DYypFxQDRSq7.9JEJOJwVNKUKxCPp.aQlbt42c56/RrAHt0ri',
     'PREMIUM', TRUE, TRUE);

-- ============================================================
-- USER PROFILES
-- ============================================================
INSERT INTO user_profiles (id, version, created_at, updated_at, auth_user_id, first_name, last_name, phone, bio, avatar_url, cover_image_url, date_of_birth, city, country, profile_public, notifications_enabled, profile_type)
VALUES
    -- Admin
    ('b0000000-0000-0000-0000-000000000001', 0, '2024-01-01 10:00:00', '2024-01-01 10:00:00',
     'a0000000-0000-0000-0000-000000000001', 'Admin', 'PetConnect', '+34 600 000 001',
     'Administrador de PetConnect. Apasionado por conectar personas con mascotas.',
     'https://res.cloudinary.com/dxjcjee8f/image/upload/v1783692382/s1evvoaojmehmibovsmo.png',
     'https://res.cloudinary.com/dxjcjee8f/image/upload/v1783692381/usiszz095a8bvfryznaz.png',
     '1990-05-15', 'Madrid', 'España', TRUE, TRUE, 'user'),

    -- Juan Pérez
    ('b0000000-0000-0000-0000-000000000002', 0, '2024-01-15 12:30:00', '2024-01-15 12:30:00',
     'a0000000-0000-0000-0000-000000000002', 'Juan', 'Pérez', '+34 612 345 678',
     'Amante de los animales y orgulloso dueño de tres mascotas. Me encanta compartir consejos sobre cuidado de perros y gatos.',
     'https://res.cloudinary.com/dxjcjee8f/image/upload/v1783692382/s1evvoaojmehmibovsmo.png',
     'https://res.cloudinary.com/dxjcjee8f/image/upload/v1783692381/usiszz095a8bvfryznaz.png',
     '1988-07-22', 'Madrid', 'España', TRUE, TRUE, 'user'),

    -- Ana García
    ('b0000000-0000-0000-0000-000000000003', 0, '2024-02-01 09:00:00', '2024-02-01 09:00:00',
     'a0000000-0000-0000-0000-000000000003', 'Ana', 'García', '+34 623 456 789',
     'Veterinaria de profesión y rescatista de animales por vocación. Actualmente tengo 4 gatos rescatados.',
     'https://res.cloudinary.com/dxjcjee8f/image/upload/v1783692382/s1evvoaojmehmibovsmo.png',
     'https://res.cloudinary.com/dxjcjee8f/image/upload/v1783692381/usiszz095a8bvfryznaz.png',
     '1992-11-03', 'Barcelona', 'España', TRUE, TRUE, 'user'),

    -- Carlos López
    ('b0000000-0000-0000-0000-000000000004', 0, '2024-02-20 16:45:00', '2024-02-20 16:45:00',
     'a0000000-0000-0000-0000-000000000004', 'Carlos', 'López', '+34 634 567 890',
     'Adiestrador canino profesional. He trabajado con más de 200 perros de todas las razas. Ofrezco consejos y sesiones de entrenamiento.',
     'https://res.cloudinary.com/dxjcjee8f/image/upload/v1783692382/s1evvoaojmehmibovsmo.png',
     'https://res.cloudinary.com/dxjcjee8f/image/upload/v1783692381/usiszz095a8bvfryznaz.png',
     '1985-03-18', 'Valencia', 'España', TRUE, TRUE, 'user'),

    -- Laura Martínez
    ('b0000000-0000-0000-0000-000000000005', 0, '2024-03-05 11:20:00', '2024-03-05 11:20:00',
     'a0000000-0000-0000-0000-000000000005', 'Laura', 'Martínez', '+34 645 678 901',
     'Fotógrafa especializada en mascotas. Capturo los momentos más especiales entre personas y sus animales.',
     'https://res.cloudinary.com/dxjcjee8f/image/upload/v1783692382/s1evvoaojmehmibovsmo.png',
     'https://res.cloudinary.com/dxjcjee8f/image/upload/v1783692381/usiszz095a8bvfryznaz.png',
     '1995-09-28', 'Sevilla', 'España', TRUE, TRUE, 'user'),

    -- Pedro Sánchez
    ('b0000000-0000-0000-0000-000000000006', 0, '2024-03-15 08:30:00', '2024-03-15 08:30:00',
     'a0000000-0000-0000-0000-000000000006', 'Pedro', 'Sánchez', '+34 656 789 012',
     'Dueño de un husky siberiano llamado Nieve. Me encanta hacer rutas de senderismo con mi perro.',
     'https://res.cloudinary.com/dxjcjee8f/image/upload/v1783692382/s1evvoaojmehmibovsmo.png',
     'https://res.cloudinary.com/dxjcjee8f/image/upload/v1783692381/usiszz095a8bvfryznaz.png',
     '1991-06-14', 'Bilbao', 'España', TRUE, TRUE, 'user'),

    -- Refugio Esperanza
    ('b0000000-0000-0000-0000-000000000010', 0, '2024-01-20 14:00:00', '2024-01-20 14:00:00',
     'a0000000-0000-0000-0000-000000000010', 'Refugio', 'Esperanza', '+34 667 890 123',
     'Refugio sin fines de lucro dedicado al cuidado de animales abandonados. Fundado en 2020 con la misión de encontrar hogares amorosos para mascotas necesitadas. Hemos rescatado y reubicado a más de 120 animales.',
     'https://res.cloudinary.com/dxjcjee8f/image/upload/v1783692382/s1evvoaojmehmibovsmo.png',
     'https://res.cloudinary.com/dxjcjee8f/image/upload/v1783692381/usiszz095a8bvfryznaz.png',
     NULL, 'Barcelona', 'España', TRUE, TRUE, 'shelter'),

    -- Dr. Carlos Martínez (vet)
    ('b0000000-0000-0000-0000-000000000011', 0, '2024-02-10 10:00:00', '2024-02-10 10:00:00',
     'a0000000-0000-0000-0000-000000000011', 'Dr. Carlos', 'Martínez', '+34 678 901 234',
     'Médico veterinario con 15 años de experiencia en animales exóticos. Especializado en aves, reptiles y pequeños mamíferos. Consultas disponibles todos los días.',
     'https://res.cloudinary.com/dxjcjee8f/image/upload/v1783692382/s1evvoaojmehmibovsmo.png',
     'https://res.cloudinary.com/dxjcjee8f/image/upload/v1783692381/usiszz095a8bvfryznaz.png',
     '1978-12-05', 'Valencia', 'España', TRUE, TRUE, 'vet'),

    -- PetShop Express
    ('b0000000-0000-0000-0000-000000000012', 0, '2024-03-01 09:30:00', '2024-03-01 09:30:00',
     'a0000000-0000-0000-0000-000000000012', 'PetShop', 'Express', '+34 689 012 345',
     'Tienda online especializada en productos para mascotas. Ofrecemos alimentos premium, juguetes interactivos y accesorios de alta calidad. Envíos a toda España en 24-48 horas.',
     'https://res.cloudinary.com/dxjcjee8f/image/upload/v1783692382/s1evvoaojmehmibovsmo.png',
     'https://res.cloudinary.com/dxjcjee8f/image/upload/v1783692381/usiszz095a8bvfryznaz.png',
     NULL, 'Sevilla', 'España', TRUE, TRUE, 'seller'),

    -- María Rodríguez (premium)
    ('b0000000-0000-0000-0000-000000000007', 0, '2024-04-01 13:00:00', '2024-04-01 13:00:00',
     'a0000000-0000-0000-0000-000000000007', 'María', 'Rodríguez', '+34 610 111 222',
     'Criadora profesional de gatos Persas e Himalayos. Todos mis ejemplares tienen pedigrí y certificados sanitarios.',
     'https://res.cloudinary.com/dxjcjee8f/image/upload/v1783692382/s1evvoaojmehmibovsmo.png',
     'https://res.cloudinary.com/dxjcjee8f/image/upload/v1783692381/usiszz095a8bvfryznaz.png',
     '1987-08-12', 'Málaga', 'España', TRUE, TRUE, 'user');

-- ============================================================
-- PETS
-- ============================================================
INSERT INTO pets (id, version, created_at, updated_at, owner_id, name, species, breed, date_of_birth, gender, bio, avatar_url, weight, weight_unit, active, microchip_id, color)
VALUES
    -- Mascotas de Juan Pérez
    ('c0000000-0000-0000-0000-000000000001', 0, '2024-01-20 14:00:00', '2024-01-20 14:00:00',
     'b0000000-0000-0000-0000-000000000002', 'Max', 'DOG', 'Golden Retriever', '2022-03-10', 'MALE',
     'Golden Retriever amante de los paseos y la playa. Muy amigable con otros perros y personas. Le encanta jugar a buscar la pelota.',
     'https://res.cloudinary.com/dxjcjee8f/image/upload/v1783692382/s1evvoaojmehmibovsmo.png',
     32.5, 'kg', TRUE, '981020001234567', 'Dorado'),

    ('c0000000-0000-0000-0000-000000000002', 0, '2024-02-01 10:00:00', '2024-02-01 10:00:00',
     'b0000000-0000-0000-0000-000000000002', 'Luna', 'CAT', 'Persa', '2021-07-22', 'FEMALE',
     'Gata persa de carácter tranquilo y cariñoso. Le gusta dormir al sol y que le rasquen la barriga.',
     'https://res.cloudinary.com/dxjcjee8f/image/upload/v1783692382/s1evvoaojmehmibovsmo.png',
     4.2, 'kg', TRUE, '981020001234568', 'Blanco'),

    ('c0000000-0000-0000-0000-000000000003', 0, '2024-02-15 16:00:00', '2024-02-15 16:00:00',
     'b0000000-0000-0000-0000-000000000002', 'Rocky', 'DOG', 'Bulldog Francés', '2023-01-05', 'MALE',
     'Bulldog francés juguetón y algo terco. Ronca muy fuerte pero es el perro más cariñoso de la casa.',
     'https://res.cloudinary.com/dxjcjee8f/image/upload/v1783692382/s1evvoaojmehmibovsmo.png',
     12.8, 'kg', TRUE, '981020001234569', 'Atigrado'),

    -- Mascotas de Ana García
    ('c0000000-0000-0000-0000-000000000004', 0, '2024-02-10 11:00:00', '2024-02-10 11:00:00',
     'b0000000-0000-0000-0000-000000000003', 'Misi', 'CAT', 'Europeo Común', '2020-11-15', 'FEMALE',
     'Gata rescatada de la calle. Muy independiente pero agradecida. Le encanta cazar moscas.',
     'https://res.cloudinary.com/dxjcjee8f/image/upload/v1783692382/s1evvoaojmehmibovsmo.png',
     3.8, 'kg', TRUE, NULL, 'Negro'),

    ('c0000000-0000-0000-0000-000000000005', 0, '2024-02-10 11:00:00', '2024-02-10 11:00:00',
     'b0000000-0000-0000-0000-000000000003', 'Garfield', 'CAT', 'Naranja', '2021-05-20', 'MALE',
     'Gato naranja glotón y dormilón. Rescatado de una perrera. Ahora vive como un rey.',
     'https://res.cloudinary.com/dxjcjee8f/image/upload/v1783692382/s1evvoaojmehmibovsmo.png',
     5.1, 'kg', TRUE, NULL, 'Naranja'),

    -- Mascotas de Carlos López
    ('c0000000-0000-0000-0000-000000000006', 0, '2024-03-01 09:00:00', '2024-03-01 09:00:00',
     'b0000000-0000-0000-0000-000000000004', 'Thor', 'DOG', 'Pastor Alemán', '2021-08-10', 'MALE',
     'Pastor Alemán de trabajo. Entrenado en obediencia básica y protección. Participa en competiciones de agility.',
     'https://res.cloudinary.com/dxjcjee8f/image/upload/v1783692382/s1evvoaojmehmibovsmo.png',
     38.0, 'kg', TRUE, '981020001234570', 'Negro y fuego'),

    ('c0000000-0000-0000-0000-000000000007', 0, '2024-03-15 10:30:00', '2024-03-15 10:30:00',
     'b0000000-0000-0000-0000-000000000004', 'Daisy', 'DOG', 'Border Collie', '2022-04-18', 'FEMALE',
     'Border Collie muy inteligente y activa. Necesita estimulación constante. Especialista en recoger el frisbee.',
     'https://res.cloudinary.com/dxjcjee8f/image/upload/v1783692382/s1evvoaojmehmibovsmo.png',
     16.5, 'kg', TRUE, '981020001234571', 'Blanco y negro'),

    -- Mascotas de Pedro Sánchez
    ('c0000000-0000-0000-0000-000000000008', 0, '2024-03-20 12:00:00', '2024-03-20 12:00:00',
     'b0000000-0000-0000-0000-000000000006', 'Nieve', 'DOG', 'Husky Siberiano', '2022-12-01', 'FEMALE',
     'Husky siberiano con ojos azules. Le encanta correr en la nieve y aullar como un lobo. Compañera de rutas de montaña.',
     'https://res.cloudinary.com/dxjcjee8f/image/upload/v1783692382/s1evvoaojmehmibovsmo.png',
     22.0, 'kg', TRUE, '981020001234572', 'Blanco y gris'),

    -- Mascotas de María Rodríguez (premium)
    ('c0000000-0000-0000-0000-000000000009', 0, '2024-04-05 15:00:00', '2024-04-05 15:00:00',
     'b0000000-0000-0000-0000-000000000007', 'Cleopatra', 'CAT', 'Persa', '2023-06-15', 'FEMALE',
     'Gata Persa de exposición. Campeona nacional en belleza felina. Carácter dulce y elegante.',
     'https://res.cloudinary.com/dxjcjee8f/image/upload/v1783692382/s1evvoaojmehmibovsmo.png',
     3.5, 'kg', TRUE, '981020001234573', 'Blanco puro'),

    ('c0000000-0000-0000-0000-000000000010', 0, '2024-04-10 16:00:00', '2024-04-10 16:00:00',
     'b0000000-0000-0000-0000-000000000007', 'Zeus', 'CAT', 'Himalayo', '2023-09-01', 'MALE',
     'Gato Himalayo de pedigrí. Ojos azules intensos. Muy tranquilo y afectuoso.',
     'https://res.cloudinary.com/dxjcjee8f/image/upload/v1783692382/s1evvoaojmehmibovsmo.png',
     4.0, 'kg', TRUE, '981020001234574', 'Crema'),

    -- Mascotas del Refugio Esperanza
    ('c0000000-0000-0000-0000-000000000011', 0, '2024-02-01 10:00:00', '2024-02-01 10:00:00',
     'b0000000-0000-0000-0000-000000000010', 'Chispa', 'DOG', 'Mestizo', '2023-05-10', 'MALE',
     'Perro mestizo rescatado de la calle. Muy agradecido y juguetón. Busca un hogar con jardín.',
     'https://res.cloudinary.com/dxjcjee8f/image/upload/v1783692382/s1evvoaojmehmibovsmo.png',
     15.0, 'kg', TRUE, '981020001234575', 'Marrón'),

    ('c0000000-0000-0000-0000-000000000012', 0, '2024-02-01 10:00:00', '2024-02-01 10:00:00',
     'b0000000-0000-0000-0000-000000000010', 'Canela', 'CAT', 'Mestizo', '2022-08-20', 'FEMALE',
     'Gata rescatada de un maltratador. Desconfiada al principio pero muy cariñosa cuando coge confianza.',
     'https://res.cloudinary.com/dxjcjee8f/image/upload/v1783692382/s1evvoaojmehmibovsmo.png',
     3.2, 'kg', TRUE, NULL, 'Canela'),

    ('c0000000-0000-0000-0000-000000000013', 0, '2024-03-10 11:00:00', '2024-03-10 11:00:00',
     'b0000000-0000-0000-0000-000000000010', 'Piolín', 'BIRD', 'Periquito', '2023-12-01', 'MALE',
     'Periquito rescatado. Muy hablador, dice varias palabras. Necesita una jaula grande y compañía.',
     'https://res.cloudinary.com/dxjcjee8f/image/upload/v1783692382/s1evvoaojmehmibovsmo.png',
     0.035, 'kg', TRUE, NULL, 'Verde y amarillo'),

    ('c0000000-0000-0000-0000-000000000014', 0, '2024-03-15 12:00:00', '2024-03-15 12:00:00',
     'b0000000-0000-0000-0000-000000000010', 'Copito', 'RABBIT', 'Cabeza de León', '2023-10-05', 'MALE',
     'Conejo cabeza de león abandonado en una jaula. Muy manso y le encanta que le acaricien.',
     'https://res.cloudinary.com/dxjcjee8f/image/upload/v1783692382/s1evvoaojmehmibovsmo.png',
     1.2, 'kg', TRUE, NULL, 'Blanco'),

    ('c0000000-0000-0000-0000-000000000015', 0, '2024-04-01 09:00:00', '2024-04-01 09:00:00',
     'b0000000-0000-0000-0000-000000000010', 'Torti', 'TURTLE', 'Tortuga de Florida', '2020-06-15', 'FEMALE',
     'Tortuga acuática rescatada de un estanque en mal estado. Necesita un acuario con filtro y luz UV.',
     'https://res.cloudinary.com/dxjcjee8f/image/upload/v1783692382/s1evvoaojmehmibovsmo.png',
     0.5, 'kg', TRUE, NULL, 'Verde oscuro'),

    ('c0000000-0000-0000-0000-000000000016', 0, '2024-04-15 14:00:00', '2024-04-15 14:00:00',
     'b0000000-0000-0000-0000-000000000010', 'Pelusa', 'HAMSTER', 'Sirio', '2024-01-20', 'FEMALE',
     'Hámster siro muy activo. Le encanta su rueda y hacer túneles. Ideal para niños responsables.',
     'https://res.cloudinary.com/dxjcjee8f/image/upload/v1783692382/s1evvoaojmehmibovsmo.png',
     0.15, 'kg', TRUE, NULL, 'Dorado');