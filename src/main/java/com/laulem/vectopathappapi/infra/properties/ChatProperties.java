package com.laulem.vectopathappapi.infra.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vecto-path.chat")
public record ChatProperties(
        String defaultSystemPrompt,
        String resourceIdsRequiredPrompt,
        String categorizerSystemPrompt,
        Integer maxContextTokens) {

    public ChatProperties {
        if (maxContextTokens == null || maxContextTokens == 0) maxContextTokens = 50000;
    }
}
