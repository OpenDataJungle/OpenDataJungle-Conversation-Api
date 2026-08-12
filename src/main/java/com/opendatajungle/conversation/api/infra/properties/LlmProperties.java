package com.opendatajungle.conversation.api.infra.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "open-data-jungle.llm")
public record LlmProperties(String modelsJson) {
}
