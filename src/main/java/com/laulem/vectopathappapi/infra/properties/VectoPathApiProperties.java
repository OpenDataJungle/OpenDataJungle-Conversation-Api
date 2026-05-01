package com.laulem.vectopathappapi.infra.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vecto-path.api")
public record VectoPathApiProperties(
        String baseUrl,
        String searchSemanticPath,
        String resourceContentPath) {
}
