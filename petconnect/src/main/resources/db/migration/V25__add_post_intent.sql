-- Intención de las publicaciones (feed con intención).
-- La columna es nullable a propósito: las publicaciones creadas antes de esta
-- migración siguen siendo válidas y se tratan como UPDATE al leerlas, por lo que
-- no se rompe la compatibilidad con clientes ni datos existentes.
ALTER TABLE posts ADD COLUMN IF NOT EXISTS intent VARCHAR(30);

CREATE INDEX IF NOT EXISTS idx_posts_intent_active
    ON posts (intent, active, created_at DESC);

-- Resolución de preguntas. El modelo elegido evita guardar un id de comentario
-- en posts: es el propio comentario el que se marca como solución. Así, cambiar
-- o retirar la selección solo requiere desmarcar el anterior y marcar el nuevo,
-- sin dejar referencias huérfanas ni necesitar una clave ajena adicional.
ALTER TABLE posts ADD COLUMN IF NOT EXISTS solved BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE comments ADD COLUMN IF NOT EXISTS is_solution BOOLEAN NOT NULL DEFAULT FALSE;

