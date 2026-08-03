package com.laulem.vectopath.conversation.api.infra.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vecto-path.knowledge-api")
public record VectoPathApiProperties(
        String baseUrl,
        String searchSemanticPath,
        String resourceContentPath) {
}
