package com.opendatajungle.conversation.api.infra.service.chatpreprocessor;

import com.opendatajungle.conversation.api.business.model.SendChatMessageCommand;
import com.opendatajungle.conversation.api.infra.model.ChatContext;
import com.opendatajungle.conversation.api.infra.model.ResourceRoutingStrategy;
import com.opendatajungle.conversation.api.infra.properties.ChatProperties;
import com.opendatajungle.conversation.api.infra.service.LlmModelKey;
import com.opendatajungle.conversation.api.infra.service.LlmModelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceCategorizationPreProcessorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    private LlmModelService llmModelService;
    @Mock
    private ChatClient chatClient;
    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;
    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;
    private ResourceCategorizationPreProcessor preProcessor;

    private ChatProperties buildChatProperties() {
        ChatProperties.PreProcessorsProperties.BasicResourceManagerProperties basicResourceManagerProperties =
                new ChatProperties.PreProcessorsProperties.BasicResourceManagerProperties(0, "required", "into-prompt", "too-long");
        ChatProperties.PreProcessorsProperties preProcessorsProperties = new ChatProperties.PreProcessorsProperties(
                new ChatProperties.PreProcessorsProperties.DefaultSystemPromptProperties("default prompt"),
                new ChatProperties.PreProcessorsProperties.ResourceCategorizationProperties("categorizer system prompt"),
                basicResourceManagerProperties);
        return new ChatProperties(1000, preProcessorsProperties);
    }

    @BeforeEach
    void setUp() {
        preProcessor = new ResourceCategorizationPreProcessor(llmModelService, objectMapper, buildChatProperties());
    }

    private SendChatMessageCommand buildCommand(List<UUID> resourceIds) {
        return new SendChatMessageCommand(UUID.randomUUID(), "hello", resourceIds, null, "default");
    }

    @Test
    void getOrder_shouldReturnConfiguredOrder() {
        // Given & When & Then
        assertThat(preProcessor.getOrder()).isEqualTo(ResourceCategorizationPreProcessor.ORDER);
    }

    @Test
    void process_shouldReturnContextUnchanged_whenResourceIdsIsEmpty() {
        // Given
        ChatContext chatContext = ChatContext.of(buildCommand(List.of()), "system");

        // When
        ChatContext result = preProcessor.process(chatContext);

        // Then
        assertThat(result).isEqualTo(chatContext);
    }

    @Test
    void process_shouldReturnContextUnchanged_whenStrategyIsNotNone() {
        // Given
        ChatContext chatContext = ChatContext.of(buildCommand(List.of(UUID.randomUUID())), "system")
                .withRoutingStrategy(ResourceRoutingStrategy.INTERNAL_SEARCH);

        // When
        ChatContext result = preProcessor.process(chatContext);

        // Then
        assertThat(result).isEqualTo(chatContext);
    }

    @Test
    void process_shouldDefaultToInternalSearch_whenNoCategorizerModelAvailable() {
        // Given
        ChatContext chatContext = ChatContext.of(buildCommand(List.of(UUID.randomUUID())), "system");
        when(llmModelService.hasCategorizerModel()).thenReturn(false);

        // When
        ChatContext result = preProcessor.process(chatContext);

        // Then
        assertThat(result.resourceRoutingStrategy()).isEqualTo(ResourceRoutingStrategy.INTERNAL_SEARCH);
    }

    @Test
    void process_shouldUseCategorizerDecision_whenCategorizerModelAvailableAndResponseValid() {
        // Given
        ChatContext chatContext = ChatContext.of(buildCommand(List.of(UUID.randomUUID())), "system");
        when(llmModelService.hasCategorizerModel()).thenReturn(true);
        when(llmModelService.getModel(LlmModelKey.CATEGORIZER)).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system("categorizer system prompt")).thenReturn(requestSpec);
        when(requestSpec.user(chatContext.userMessage())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("{\"strategy\":\"INCLUDE_IN_PROMPT\"}");

        // When
        ChatContext result = preProcessor.process(chatContext);

        // Then
        assertThat(result.resourceRoutingStrategy()).isEqualTo(ResourceRoutingStrategy.INCLUDE_IN_PROMPT);
    }

    @Test
    void process_shouldFallBackToInternalSearch_whenCategorizerResponseIsNotValidJson() {
        // Given
        ChatContext chatContext = ChatContext.of(buildCommand(List.of(UUID.randomUUID())), "system");
        when(llmModelService.hasCategorizerModel()).thenReturn(true);
        when(llmModelService.getModel(LlmModelKey.CATEGORIZER)).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system("categorizer system prompt")).thenReturn(requestSpec);
        when(requestSpec.user(chatContext.userMessage())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("not-json");

        // When
        ChatContext result = preProcessor.process(chatContext);

        // Then
        assertThat(result.resourceRoutingStrategy()).isEqualTo(ResourceRoutingStrategy.INTERNAL_SEARCH);
    }

    @Test
    void getStrategy_shouldDefaultToInternalSearch_whenDecisionStrategyMissingFromJson() {
        // Given
        when(llmModelService.getModel(LlmModelKey.CATEGORIZER)).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system("categorizer system prompt")).thenReturn(requestSpec);
        when(requestSpec.user("user message")).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("{}");

        // When
        String strategy = preProcessor.getStrategy("user message");

        // Then
        assertThat(strategy).isEqualTo(ResourceRoutingStrategy.INTERNAL_SEARCH);
    }
}
