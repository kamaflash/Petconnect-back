-- Etiquetas de las publicaciones (tags).
-- La columna es nullable para no romper publicaciones existentes
-- ni requerir rellenar valores por defecto en filas previas.
ALTER TABLE posts ADD COLUMN IF NOT EXISTS tags VARCHAR(2000);