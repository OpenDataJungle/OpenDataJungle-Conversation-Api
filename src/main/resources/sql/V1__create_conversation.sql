-- V1: Create conversation table
-- Run manually or via migration tool (Liquibase/Flyway)

CREATE TABLE conversation
(
    id             UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    user_id        VARCHAR(255) NOT NULL,
    title          VARCHAR(500),
    system_message TEXT,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_conversation_user_id ON conversation (user_id);

