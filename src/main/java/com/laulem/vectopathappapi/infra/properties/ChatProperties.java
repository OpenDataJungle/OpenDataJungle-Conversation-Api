package com.laulem.vectopathappapi.infra.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "vecto-path.chat")
public class ChatProperties {
    private String defaultSystemPrompt;
    private String resourceIdsRequiredPrompt;
    private int maxContextTokens = 50000;
}

