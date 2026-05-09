package com.laulem.vectopathappapi.infra.conf;

import com.laulem.vectopathappapi.infra.properties.ChatProperties;
import com.laulem.vectopathappapi.infra.service.chatpreprocessor.BasicResourceManagerPreProcessor;
import com.laulem.vectopathappapi.infra.service.chatpreprocessor.ChatPreProcessor;
import com.laulem.vectopathappapi.infra.service.chatpreprocessor.DefaultSystemPromptPreProcessor;
import com.laulem.vectopathappapi.infra.service.chatpreprocessor.ResourceCategorizationPreProcessor;
import com.laulem.vectopathappapi.infra.service.LlmModelService;
import com.laulem.vectopathappapi.infra.service.ResourceContentService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class ChatPreProcessorConfiguration {
    @Bean
    @ConditionalOnProperty(name = "vecto-path.chat.pre-processors.default-system-prompt.enabled", havingValue = "true", matchIfMissing = true)
    public ChatPreProcessor defaultSystemPromptPreProcessor(ChatProperties chatProperties) {
        return new DefaultSystemPromptPreProcessor(chatProperties);
    }

    @Bean
    @ConditionalOnProperty(name = "vecto-path.chat.pre-processors.resource-categorization.enabled", havingValue = "true", matchIfMissing = true)
    public ChatPreProcessor resourceCategorizationPreProcessor(LlmModelService llmModelService, ObjectMapper objectMapper, ChatProperties chatProperties) {
        return new ResourceCategorizationPreProcessor(llmModelService, objectMapper, chatProperties);
    }

    @Bean
    @ConditionalOnProperty(name = "vecto-path.chat.pre-processors.basic-resource-manager.enabled", havingValue = "true", matchIfMissing = true)
    public ChatPreProcessor basicResourceManagerPreProcessor(ResourceContentService resourceContentService, ChatProperties chatProperties) {
        return new BasicResourceManagerPreProcessor(resourceContentService, chatProperties);
    }
}
