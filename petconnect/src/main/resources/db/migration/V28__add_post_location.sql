-- Localización de las publicaciones (pérdidas, quedadas...).
--
-- location guarda la dirección confirmada en el selector; latitude/longitude
-- son opcionales y solo se rellenan cuando el usuario confirma en el mapa,
-- igual que en el perfil de usuario. Las tres columnas son NULL a propósito:
-- las publicaciones creadas antes de esta migración siguen siendo válidas y
-- simplemente se muestran sin ubicación.
ALTER TABLE posts ADD COLUMN IF NOT EXISTS location VARCHAR(500);
ALTER TABLE posts ADD COLUMN IF NOT EXISTS latitude DOUBLE PRECISION;
ALTER TABLE posts ADD COLUMN IF NOT EXISTS longitude DOUBLE PRECISION;
