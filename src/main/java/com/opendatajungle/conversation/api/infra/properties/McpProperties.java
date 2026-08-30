package com.opendatajungle.conversation.api.infra.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "open-data-jungle.mcp")
public record McpProperties(String serverJson, int requestTimeoutSeconds) {

    public McpProperties {
        if (requestTimeoutSeconds <= 0) requestTimeoutSeconds = 30;
    }
}
