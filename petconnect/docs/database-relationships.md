# Relaciones de Base de Datos - PetConnect

## Diagrama de Relaciones

```
auth_users (1) ──────┐
                     │ (1:1)
                     ▼
              user_profiles (1) ──────┐
                     │ (1:N)         │ (1:N)
                     ▼               ▼
                   pets          posts
                                  │ (1:N)
                                  ▼
                               stories
```

## Tablas y Relaciones

### 1. auth_users
- **PK**: `id`
- **Relaciones**:
  - → user_profiles (1:1) via `auth_user_id`

### 2. user_profiles
- **PK**: `id`
- **FKs**:
  - `auth_user_id` → auth_users(id) [ON DELETE CASCADE]
- **Relaciones**:
  - ← auth_users (1:1)
  - → pets (1:N) via `id`
  - → posts (1:N) via `id`
  - → stories (1:N) via `id`

### 3. pets
- **PK**: `id`
- **FKs**:
  - `owner_id` → user_profiles(id) [ON DELETE CASCADE]
- **Relaciones**:
  - ← user_profiles (N:1)
- **Campos médicos y de salud**:
  - `blood_type`, `allergies`, `medical_conditions`
  - `vet_name`, `vet_phone`, `vet_address`
  - `last_vaccination_date`, `next_vaccination_date`, `last_vet_visit`
  - `insurance_provider`, `insurance_policy_number`
- **Campos adicionales**:
  - `emergency_contact`, `emergency_phone`
  - `adoption_date`, `adoption_center`
  - `registration_number`, `license_number`, `license_expiry_date`
  - `spayed_neutered`, `spayed_neutered_date`
  - `deceased`, `date_deceased`
- **Campos de comportamiento**:
  - `temperament`, `energy_level`, `training_level`
  - `favorite_activities`, `favorite_food`, `special_needs`
- **Campos de ubicación y seguridad**:
  - `last_known_location`, `lost`, `lost_date`

### 4. posts
- **PK**: `id`
- **FKs**:
  - `author_id` → user_profiles(id) [ON DELETE CASCADE]
- **Relaciones**:
  - ← user_profiles (N:1)

### 5. stories
- **PK**: `id`
- **FKs**:
  - `user_id` → user_profiles(id) [ON DELETE CASCADE]
- **Relaciones**:
  - ← user_profiles (N:1)

## Migraciones

### V1__initial_schema.sql
- Crea tablas base: auth_users, user_profiles, pets
- Define índices pero NO define todas las FKs

### V2__seed_data.sql
- Inserta datos de prueba con relaciones correctas
- auth_users: 9 usuarios (admin, usuarios, refugio, vet, seller)
- user_profiles: 9 perfiles vinculados a auth_users
- pets: 16 mascotas distribuidas entre los usuarios

### V3__add_posts_and_stories.sql
- Crea tablas: posts, stories
- Define FKs: posts.author_id → user_profiles, stories.user_id → user_profiles
- Inserta posts e historias vinculadas a perfiles

### V4__add_foreign_keys.sql ⭐ (NUEVO)
- Agrega FKs faltantes:
  - user_profiles.auth_user_id → auth_users(id)
  - pets.owner_id → user_profiles(id)
- Todas las FKs usan ON DELETE CASCADE

## Integridad Referencial

✅ **Todas las tablas tienen relaciones definidas**
✅ **ON DELETE CASCADE en todas las FKs** (eliminación en cascada)
✅ **Datos de prueba respetan las relaciones**
✅ **Índices en columnas FK para optimizar consultas**

## Comportamiento en Eliminación

- Si se elimina un **auth_user** → se elimina su **user_profile**
- Si se elimina un **user_profile** → se eliminan:
  - Sus **pets**
  - Sus **posts**
  - Sus **stories**

## Notas

- Todos los IDs son UUIDs generados
- Los datos de prueba usan IDs predecibles (a000..., b000..., c000..., etc.)
- Las contraseñas están hasheadas con BCrypt
- El schema usa Flyway para migraciones versionadas