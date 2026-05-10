package com.laulem.vectopathappapi.infra.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laulem.vectopathappapi.business.service.AuthenticationService;
import com.laulem.vectopathappapi.business.service.ChatService;
import com.laulem.vectopathappapi.infra.properties.LlmProperties;
import com.laulem.vectopathappapi.infra.properties.McpProperties;
import com.laulem.vectopathappapi.infra.properties.VectoPathApiProperties;
import com.laulem.vectopathappapi.infra.service.ChatServiceImpl;
import com.laulem.vectopathappapi.infra.service.LlmModelService;
import com.laulem.vectopathappapi.infra.service.LlmModelServiceImpl;
import com.laulem.vectopathappapi.infra.service.McpClientService;
import com.laulem.vectopathappapi.infra.service.McpClientServiceImpl;
import com.laulem.vectopathappapi.infra.service.ResourceContentService;
import com.laulem.vectopathappapi.infra.service.ResourceContentServiceImpl;
import com.laulem.vectopathappapi.infra.service.SearchService;
import com.laulem.vectopathappapi.infra.service.SearchServiceImpl;
import com.laulem.vectopathappapi.infra.service.SecurityContextAuthenticationService;
import com.laulem.vectopathappapi.infra.service.chatpreprocessor.ChatPreProcessingOrchestrator;
import com.laulem.vectopathappapi.infra.tool.ChatRequestHolder;
import com.laulem.vectopathappapi.infra.tool.SemanticSearchTool;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class InfraServicesConfiguration {

    @Bean
    @ConditionalOnMissingBean(AuthenticationService.class)
    public AuthenticationService authenticationService() {
        return new SecurityContextAuthenticationService();
    }

    @Bean
    @ConditionalOnMissingBean(LlmModelService.class)
    public LlmModelService llmModelService(LlmProperties llmProperties, ObjectMapper objectMapper) {
        return new LlmModelServiceImpl(llmProperties, objectMapper);
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

