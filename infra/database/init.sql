-- ====================================================================
-- OpenDataJungle Database Initialization Script (Test Environment)
-- ====================================================================

CREATE SCHEMA IF NOT EXISTS conversation;
SET search_path TO conversation, public;

DROP TABLE IF EXISTS conversation.message;
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

CREATE TABLE conversation.message
(
    id              UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    conversation_id UUID        NOT NULL REFERENCES conversation.conversation (id) ON DELETE CASCADE,
    type            VARCHAR(20) NOT NULL, -- USER | ASSISTANT | SYSTEM
    tool_results    JSONB,
    content         TEXT        NOT NULL,
    in_context      BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_conv_message_conversation_id ON conversation.message (conversation_id, created_at);

