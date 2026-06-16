package com.laulem.vectopath.conversation.api.business.model;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record SendChatMessageCommand(
        UUID conversationId,
        String message,
        List<UUID> resourceIds,
        Set<String> enabledTools,
        String llmModel
) {
}
