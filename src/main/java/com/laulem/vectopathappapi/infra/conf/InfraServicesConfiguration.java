package com.laulem.vectopathappapi.infra.conf;

import com.laulem.vectopathappapi.infra.properties.ChatProperties;
import com.laulem.vectopathappapi.infra.service.ChatPreProcessor;
import com.laulem.vectopathappapi.infra.service.LlmModelService;
import com.laulem.vectopathappapi.infra.service.ResourceCategorizerService;
import com.laulem.vectopathappapi.infra.service.ResourceContentService;
import com.laulem.vectopathappapi.infra.service.ResourceResourceRoutingPreProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfraServicesConfiguration {

    @Bean
    @ConditionalOnProperty(
            name = "vecto-path.chat.resource-routing.enabled",
            havingValue = "true",
            matchIfMissing = true)
    public ChatPreProcessor resourceRoutingPreProcessor(
            LlmModelService llmModelService,
            ResourceCategorizerService resourceCategorizerService,
            ResourceContentService resourceContentService,
            ChatProperties chatProperties) {
        return new ResourceResourceRoutingPreProcessor(
                llmModelService,
                resourceCategorizerService,
                resourceContentService,
                chatProperties);
    }
}
