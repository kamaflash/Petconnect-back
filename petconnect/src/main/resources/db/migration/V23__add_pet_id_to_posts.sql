-- Vincula publicaciones del feed con la mascota que anuncian (p. ej. puesta en adopción).
-- pet_id es nullable: los posts manuales existentes no están vinculados a ninguna mascota.
DO $$
BEGIN
    IF to_regclass('public.posts') IS NOT NULL
       AND NOT EXISTS (
           SELECT 1 FROM information_schema.columns
           WHERE table_schema = 'public' AND table_name = 'posts' AND column_name = 'pet_id'
       ) THEN
        ALTER TABLE posts ADD COLUMN pet_id UUID REFERENCES pets(id) ON DELETE SET NULL;
        CREATE INDEX IF NOT EXISTS idx_posts_pet_id ON posts(pet_id);
    END IF;
END $$;
