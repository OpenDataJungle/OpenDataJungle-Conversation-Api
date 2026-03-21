package com.laulem.vectopathappapi.infra.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "security.scopes")
@Data
public class SecurityScopesProperties {
    private Conversations conversations = new Conversations();

    @Data
    public static class Conversations {
        private String read;
        private String write;
        private String delete;
        private String admin;
    }
}
