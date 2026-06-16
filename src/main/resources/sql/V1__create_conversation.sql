CREATE SCHEMA IF NOT EXISTS conversation;
SET search_path TO conversation, public;

DROP TABLE IF EXISTS conversation.conversation;

CREATE TABLE conversation.conversation
(
    id             UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    user_id        VARCHAR(255) NOT NULL,
    title          VARCHAR(500),
    system_message TEXT,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_message_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_conversation_user_id ON conversation.conversation (user_id);

