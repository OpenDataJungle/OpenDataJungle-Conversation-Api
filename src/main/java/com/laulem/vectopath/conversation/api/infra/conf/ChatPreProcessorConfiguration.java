package com.laulem.vectopath.conversation.api.infra.conf;

import com.laulem.vectopath.conversation.api.infra.properties.ChatProperties;
import com.laulem.vectopath.conversation.api.infra.service.chatpreprocessor.BasicResourceManagerPreProcessor;
import com.laulem.vectopath.conversation.api.infra.service.chatpreprocessor.ChatPreProcessingOrchestrator;
import com.laulem.vectopath.conversation.api.infra.service.chatpreprocessor.ChatPreProcessingOrchestratorImpl;
import com.laulem.vectopath.conversation.api.infra.service.chatpreprocessor.ChatPreProcessor;
import com.laulem.vectopath.conversation.api.infra.service.chatpreprocessor.DefaultSystemPromptPreProcessor;
import com.laulem.vectopath.conversation.api.infra.service.chatpreprocessor.ResourceCategorizationPreProcessor;
import com.laulem.vectopath.conversation.api.infra.service.LlmModelService;
import com.laulem.vectopath.conversation.api.infra.service.ResourceContentService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

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

    @Bean
    @ConditionalOnMissingBean(ChatPreProcessingOrchestrator.class)
    public ChatPreProcessingOrchestrator chatPreProcessingOrchestrator(List<ChatPreProcessor> processors) {
        return new ChatPreProcessingOrchestratorImpl(processors);
    }
}
