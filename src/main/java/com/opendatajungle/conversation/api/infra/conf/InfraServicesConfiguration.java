package com.opendatajungle.conversation.api.infra.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opendatajungle.commons.business.service.AuthenticationUseCase;
import com.opendatajungle.conversation.api.business.service.ChatService;
import com.opendatajungle.conversation.api.infra.properties.LlmProperties;
import com.opendatajungle.conversation.api.infra.properties.McpProperties;
import com.opendatajungle.conversation.api.infra.properties.OpenDataJungleKnowledgeApiProperties;
import com.opendatajungle.conversation.api.infra.service.ChatServiceImpl;
import com.opendatajungle.conversation.api.infra.service.LlmModelService;
import com.opendatajungle.conversation.api.infra.service.LlmModelServiceImpl;
import com.opendatajungle.conversation.api.infra.service.McpClientService;
import com.opendatajungle.conversation.api.infra.service.McpClientServiceImpl;
import com.opendatajungle.conversation.api.infra.service.ResourceContentService;
import com.opendatajungle.conversation.api.infra.service.ResourceContentServiceImpl;
import com.opendatajungle.conversation.api.infra.service.SearchService;
import com.opendatajungle.conversation.api.infra.service.SearchServiceImpl;
import com.opendatajungle.conversation.api.infra.service.chatpreprocessor.ChatPreProcessingOrchestrator;
import com.opendatajungle.conversation.api.infra.service.factory.ChatClientFactory;
import com.opendatajungle.conversation.api.infra.service.factory.OllamaChatClientFactory;
import com.opendatajungle.conversation.api.infra.service.factory.OpenAiChatClientFactory;
import com.opendatajungle.conversation.api.infra.tool.ChatRequestHolder;
import com.opendatajungle.conversation.api.infra.tool.SemanticSearchTool;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.util.List;

@Configuration
public class InfraServicesConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "open-data-jungle.llm.default-providers.openai", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ChatClientFactory openAiChatClientFactory() {
        return new OpenAiChatClientFactory();
    }

    @Bean
    @ConditionalOnProperty(prefix = "open-data-jungle.llm.default-providers.ollama", name = "enabled", havingValue = "true")
    public ChatClientFactory ollamaChatClientFactory() {
        return new OllamaChatClientFactory();
    }

    @Bean
    @ConditionalOnMissingBean(LlmModelService.class)
    public LlmModelService llmModelService(LlmProperties llmProperties, ObjectMapper objectMapper, List<ChatClientFactory> chatClientFactories) {
        return new LlmModelServiceImpl(llmProperties, objectMapper, chatClientFactories);
    }

    @Bean
    @ConditionalOnMissingBean(SearchService.class)
    public SearchService searchService(
            RestClient.Builder restClientBuilder,
            OpenDataJungleKnowledgeApiProperties openDataJungleKnowledgeApiProperties,
            AuthenticationUseCase authenticationService) {
        return new SearchServiceImpl(restClientBuilder, openDataJungleKnowledgeApiProperties, authenticationService);
    }

    @Bean
    @ConditionalOnMissingBean(ResourceContentService.class)
    public ResourceContentService resourceContentService(
            RestClient.Builder restClientBuilder,
            OpenDataJungleKnowledgeApiProperties properties,
            AuthenticationUseCase authenticationService) {
        return new ResourceContentServiceImpl(restClientBuilder, properties, authenticationService);
    }

    @Bean
    @ConditionalOnMissingBean(McpClientService.class)
    public McpClientService mcpClientService(
            McpProperties mcpProperties,
            ObjectMapper objectMapper,
            ChatRequestHolder chatRequestHolder) {
        return new McpClientServiceImpl(mcpProperties, objectMapper, chatRequestHolder);
    }

    @Bean
    @ConditionalOnMissingBean(ChatService.class)
    public ChatService chatService(
            LlmModelService llmModelService,
            SemanticSearchTool semanticSearchTool,
            McpClientService mcpClientService,
            ChatMemory chatMemory,
            ChatRequestHolder chatRequestHolder,
            ChatPreProcessingOrchestrator chatPreProcessingOrchestrator) {
        return new ChatServiceImpl(llmModelService, semanticSearchTool, mcpClientService, chatMemory, chatRequestHolder, chatPreProcessingOrchestrator);
    }
}

