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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InfraServicesConfigurationTest {

    @Mock
    private RestClient.Builder restClientBuilder;

    @Mock
    private RestClient restClient;

    @Mock
    private AuthenticationUseCase authenticationService;

    @Mock
    private OpenDataJungleKnowledgeApiProperties openDataJungleKnowledgeApiProperties;

    @Mock
    private LlmModelService llmModelService;

    @Mock
    private SemanticSearchTool semanticSearchTool;

    @Mock
    private McpClientService mcpClientService;

    @Mock
    private ChatMemory chatMemory;

    @Mock
    private ChatRequestHolder chatRequestHolder;

    @Mock
    private ChatPreProcessingOrchestrator chatPreProcessingOrchestrator;

    private final InfraServicesConfiguration configuration = new InfraServicesConfiguration();

    @Test
    void openAiChatClientFactory_shouldReturnOpenAiChatClientFactoryInstance() {
        // Given & When
        ChatClientFactory result = configuration.openAiChatClientFactory();

        // Then
        assertThat(result).isInstanceOf(OpenAiChatClientFactory.class);
    }

    @Test
    void ollamaChatClientFactory_shouldReturnOllamaChatClientFactoryInstance() {
        // Given & When
        ChatClientFactory result = configuration.ollamaChatClientFactory();

        // Then
        assertThat(result).isInstanceOf(OllamaChatClientFactory.class);
    }

    @Test
    void llmModelService_shouldReturnLlmModelServiceImplInstance() {
        // Given
        LlmProperties llmProperties = new LlmProperties(
                "{\"default\":{\"provider\":\"openai\",\"apiKey\":\"key\",\"baseUrl\":null,\"model\":\"gpt\",\"name\":\"default\",\"options\":{}}}");
        ObjectMapper objectMapper = new ObjectMapper();
        OpenAiChatClientFactory openAiChatClientFactory = new OpenAiChatClientFactory();

        // When
        LlmModelService result = configuration.llmModelService(llmProperties, objectMapper, List.of(openAiChatClientFactory));

        // Then
        assertThat(result).isInstanceOf(LlmModelServiceImpl.class);
    }

    @Test
    void searchService_shouldReturnSearchServiceImplInstance() {
        // Given
        when(restClientBuilder.baseUrl(anyString())).thenReturn(restClientBuilder);
        when(restClientBuilder.build()).thenReturn(restClient);
        when(openDataJungleKnowledgeApiProperties.baseUrl()).thenReturn("http://laulem.com");

        // When
        SearchService result = configuration.searchService(restClientBuilder, openDataJungleKnowledgeApiProperties, authenticationService);

        // Then
        assertThat(result).isInstanceOf(SearchServiceImpl.class);
    }

    @Test
    void resourceContentService_shouldReturnResourceContentServiceImplInstance() {
        // Given
        when(restClientBuilder.baseUrl(anyString())).thenReturn(restClientBuilder);
        when(restClientBuilder.build()).thenReturn(restClient);
        when(openDataJungleKnowledgeApiProperties.baseUrl()).thenReturn("http://laulem.com");

        // When
        ResourceContentService result = configuration.resourceContentService(restClientBuilder, openDataJungleKnowledgeApiProperties, authenticationService);

        // Then
        assertThat(result).isInstanceOf(ResourceContentServiceImpl.class);
    }

    @Test
    void mcpClientService_shouldReturnMcpClientServiceImplInstance() {
        // Given
        McpProperties mcpProperties = new McpProperties(null);
        ObjectMapper objectMapper = new ObjectMapper();

        // When
        McpClientService result = configuration.mcpClientService(mcpProperties, objectMapper, chatRequestHolder);

        // Then
        assertThat(result).isInstanceOf(McpClientServiceImpl.class);
    }

    @Test
    void chatService_shouldReturnChatServiceImplInstance() {
        // Given & When
        ChatService result = configuration.chatService(
                llmModelService, semanticSearchTool, mcpClientService, chatMemory, chatRequestHolder, chatPreProcessingOrchestrator);

        // Then
        assertThat(result).isInstanceOf(ChatServiceImpl.class);
    }
}
