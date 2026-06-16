package com.laulem.vectopath.conversation.api.infra.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laulem.vectopath.conversation.api.business.service.AuthenticationService;
import com.laulem.vectopath.conversation.api.business.service.ChatService;
import com.laulem.vectopath.conversation.api.infra.properties.LlmProperties;
import com.laulem.vectopath.conversation.api.infra.properties.McpProperties;
import com.laulem.vectopath.conversation.api.infra.properties.VectoPathApiProperties;
import com.laulem.vectopath.conversation.api.infra.service.ChatServiceImpl;
import com.laulem.vectopath.conversation.api.infra.service.LlmModelService;
import com.laulem.vectopath.conversation.api.infra.service.LlmModelServiceImpl;
import com.laulem.vectopath.conversation.api.infra.service.McpClientService;
import com.laulem.vectopath.conversation.api.infra.service.McpClientServiceImpl;
import com.laulem.vectopath.conversation.api.infra.service.ResourceContentService;
import com.laulem.vectopath.conversation.api.infra.service.ResourceContentServiceImpl;
import com.laulem.vectopath.conversation.api.infra.service.SearchService;
import com.laulem.vectopath.conversation.api.infra.service.SearchServiceImpl;
import com.laulem.vectopath.conversation.api.infra.service.SecurityContextAuthenticationService;
import com.laulem.vectopath.conversation.api.infra.service.chatpreprocessor.ChatPreProcessingOrchestrator;
import com.laulem.vectopath.conversation.api.infra.service.factory.ChatClientFactory;
import com.laulem.vectopath.conversation.api.infra.service.factory.OllamaChatClientFactory;
import com.laulem.vectopath.conversation.api.infra.service.factory.OpenAiChatClientFactory;
import com.laulem.vectopath.conversation.api.infra.tool.ChatRequestHolder;
import com.laulem.vectopath.conversation.api.infra.tool.SemanticSearchTool;
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
    @ConditionalOnMissingBean(AuthenticationService.class)
    public AuthenticationService authenticationService() {
        return new SecurityContextAuthenticationService();
    }

    @Bean
    @ConditionalOnProperty(prefix = "vecto-path.llm.default-providers.openai", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ChatClientFactory openAiChatClientFactory() {
        return new OpenAiChatClientFactory();
    }

    @Bean
    @ConditionalOnProperty(prefix = "vecto-path.llm.default-providers.ollama", name = "enabled", havingValue = "true")
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
            VectoPathApiProperties vectoPathApiProperties,
            AuthenticationService authenticationService) {
        return new SearchServiceImpl(restClientBuilder, vectoPathApiProperties, authenticationService);
    }

    @Bean
    @ConditionalOnMissingBean(ResourceContentService.class)
    public ResourceContentService resourceContentService(
            RestClient.Builder restClientBuilder,
            VectoPathApiProperties properties,
            AuthenticationService authenticationService) {
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

