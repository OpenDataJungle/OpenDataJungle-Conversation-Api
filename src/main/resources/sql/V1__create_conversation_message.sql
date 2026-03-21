-- V2: Create conversation_message table
-- Messages are persisted per conversation and loaded by JpaChatMemoryRepository
-- to feed Spring AI ChatMemory on each chat turn.

CREATE TABLE conversation_message
(
    id              UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    conversation_id UUID        NOT NULL
        REFERENCES conversation (id) ON DELETE CASCADE,
    type            VARCHAR(20) NOT NULL, -- USER | ASSISTANT | SYSTEM
    tool_results    JSONB,
    content         TEXT        NOT NULL,
    in_context      BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_conv_message_conversation_id ON conversation_message (conversation_id, created_at);

