-- Add location, latitude and longitude columns to user_profiles
ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS location VARCHAR(500);
ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS latitude DOUBLE PRECISION;
ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS longitude DOUBLE PRECISION;

-- Migrate existing data: combine city and country into location
UPDATE user_profiles SET location = CONCAT_WS(', ', city, country) WHERE city IS NOT NULL OR country IS NOT NULL;