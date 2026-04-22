package com.laulem.vectopathappapi.infra.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.scopes")
public record SecurityScopesProperties(Conversations conversations) {

    public record Conversations(String read, String write, String delete, String admin) {
    }
}
