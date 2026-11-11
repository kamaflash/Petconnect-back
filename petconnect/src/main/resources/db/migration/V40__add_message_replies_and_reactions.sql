ALTER TABLE messages
    ADD COLUMN reply_to_message_id UUID REFERENCES messages(id) ON DELETE SET NULL;

CREATE INDEX idx_messages_reply_to ON messages (reply_to_message_id);

CREATE TABLE message_reactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id UUID NOT NULL REFERENCES messages(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    emoji VARCHAR(16) NOT NULL,
    CONSTRAINT uk_message_reaction_user_emoji UNIQUE (message_id, user_id, emoji)
);

CREATE INDEX idx_message_reactions_message ON message_reactions (message_id);
