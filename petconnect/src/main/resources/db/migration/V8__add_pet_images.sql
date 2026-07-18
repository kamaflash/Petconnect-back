-- Pet images table for multiple avatar images per pet
CREATE TABLE IF NOT EXISTS pet_images (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    pet_id UUID NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    sort_order INTEGER NOT NULL
);

CREATE INDEX idx_pet_images_pet_id ON pet_images(pet_id);
CREATE INDEX idx_pet_images_sort_order ON pet_images(sort_order);