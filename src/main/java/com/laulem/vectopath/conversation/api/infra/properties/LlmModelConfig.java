package com.laulem.vectopath.conversation.api.infra.properties;

import java.util.Map;

public record LlmModelConfig(
        String provider,
        String apiKey,
        String baseUrl,
        String model,
        String name,
        Map<String, Object> options) {
}
