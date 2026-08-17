-- ============================================================
-- PET OWNERSHIP HISTORY (Historial de propietarios de mascotas)
-- ============================================================
CREATE TABLE IF NOT EXISTS pet_ownership_history (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    pet_id UUID NOT NULL,
    previous_owner_id UUID NOT NULL,
    new_owner_id UUID NOT NULL,
    reason VARCHAR(30) NOT NULL,
    adoption_request_id UUID
);

CREATE INDEX idx_ownership_history_pet ON pet_ownership_history(pet_id);
CREATE INDEX idx_ownership_history_previous_owner ON pet_ownership_history(previous_owner_id);
CREATE INDEX idx_ownership_history_new_owner ON pet_ownership_history(new_owner_id);