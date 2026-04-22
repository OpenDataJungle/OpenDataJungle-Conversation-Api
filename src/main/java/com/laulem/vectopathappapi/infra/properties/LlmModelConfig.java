package com.laulem.vectopathappapi.infra.properties;

public record LlmModelConfig(
        String provider,
        String apiKey,
        String baseUrl,
        String model,
        String name) {
}
