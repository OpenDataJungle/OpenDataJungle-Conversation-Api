package com.opendatajungle.conversation.api.business.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * @param type Message type: USER | ASSISTANT | SYSTEM
 */
public record ConversationMessage(
        UUID id,
        UUID conversationId,
        String type,
        String content,
        Instant createdAt,
        List<ToolResult> toolResults) {
}
