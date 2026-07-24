-- Add available_for_adoption column to pets table
ALTER TABLE pets ADD COLUMN IF NOT EXISTS available_for_adoption BOOLEAN NOT NULL DEFAULT FALSE;

-- Add pet_id column to adoptions table
ALTER TABLE adoptions ADD COLUMN IF NOT EXISTS pet_id UUID;

-- Update existing adoptions to set pet_id from the pet name (for existing data)
-- This is a best-effort update; new records will have pet_id set properly
UPDATE adoptions SET pet_id = (SELECT id FROM pets WHERE pets.name = adoptions.pet_name LIMIT 1) WHERE pet_id IS NULL;