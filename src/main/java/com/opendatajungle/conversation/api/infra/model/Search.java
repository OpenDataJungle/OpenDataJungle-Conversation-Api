package com.opendatajungle.conversation.api.infra.model;

import java.time.Instant;
import java.util.UUID;

public record Search(
        UUID vectorId,
        UUID resourceId,
        String resourceName,
        String content,
        String contentType,
        String metadata,
        Double similarityScore,
        Instant createdAt,
        Instant updatedAt) {
}
