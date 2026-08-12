package com.opendatajungle.conversation.api.infra.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "open-data-jungle.knowledge-api")
public record OpenDataJungleKnowledgeApiProperties(
        String baseUrl,
        String searchSemanticPath,
        String resourceContentPath) {
}
