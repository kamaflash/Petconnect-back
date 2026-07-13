-- Add missing foreign key constraints for data integrity

-- ============================================================
-- USER_PROFILES -> AUTH_USERS relationship
-- ============================================================
ALTER TABLE user_profiles
ADD CONSTRAINT fk_user_profiles_auth_user_id
    FOREIGN KEY (auth_user_id)
    REFERENCES auth_users(id)
    ON DELETE CASCADE;

-- ============================================================
-- PETS -> USER_PROFILES relationship
-- ============================================================
ALTER TABLE pets
ADD CONSTRAINT fk_pets_owner_id
    FOREIGN KEY (owner_id)
    REFERENCES user_profiles(id)
    ON DELETE CASCADE;

-- ============================================================
-- Verify constraints (optional, for documentation)
-- ============================================================
-- This migration ensures:
-- 1. Every user profile must be linked to a valid auth user
-- 2. If an auth user is deleted, their profile is also deleted (CASCADE)
-- 3. Every pet must belong to a valid user profile
-- 4. If a user profile is deleted, all their pets are also deleted (CASCADE)
-- 5. Posts and stories already have their FK constraints from V3
