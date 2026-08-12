package com.opendatajungle.conversation.api.infra.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "security.scopes")
public class SecurityScopesProperties {
    private Conversations conversations;

    @Getter
    @Setter
    public static class Conversations {
        private String read;
        private String write;
        private String delete;
        private String admin;
    }
}
