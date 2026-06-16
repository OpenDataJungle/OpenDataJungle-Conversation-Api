package com.laulem.vectopath.conversation.api.infra.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vecto-path.llm")
public record LlmProperties(String modelsJson) {
}
