package com.laulem.vectopathappapi.infra.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "vecto-path.llm")
public class LlmProperties {
    private String modelsJson;
}
