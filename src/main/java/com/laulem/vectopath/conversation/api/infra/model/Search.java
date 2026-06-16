package com.laulem.vectopath.conversation.api.infra.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record Search(
        UUID vectorId,
        UUID resourceId,
        String resourceName,
        String content,
        String contentType,
        String metadata,
        Double similarityScore,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
