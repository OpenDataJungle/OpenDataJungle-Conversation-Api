package com.laulem.vectopathappapi.business.model;

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
