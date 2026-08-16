package com.opendatajungle.conversation.api.infra.conf;

import com.opendatajungle.conversation.api.infra.properties.ChatProperties;
import com.opendatajungle.conversation.api.infra.service.LlmModelService;
import com.opendatajungle.conversation.api.infra.service.ResourceContentService;
import com.opendatajungle.conversation.api.infra.service.chatpreprocessor.BasicResourceManagerPreProcessor;
import com.opendatajungle.conversation.api.infra.service.chatpreprocessor.ChatPreProcessingOrchestrator;
import com.opendatajungle.conversation.api.infra.service.chatpreprocessor.ChatPreProcessingOrchestratorImpl;
import com.opendatajungle.conversation.api.infra.service.chatpreprocessor.ChatPreProcessor;
import com.opendatajungle.conversation.api.infra.service.chatpreprocessor.DefaultSystemPromptPreProcessor;
import com.opendatajungle.conversation.api.infra.service.chatpreprocessor.ResourceCategorizationPreProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ChatPreProcessorConfigurationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChatPreProcessorConfiguration configuration = new ChatPreProcessorConfiguration();
    @Mock
    private ResourceContentService resourceContentService;
    @Mock
    private LlmModelService llmModelService;
    @Mock
    private ChatPreProcessor firstProcessor;
    @Mock
    private ChatPreProcessor secondProcessor;

    private ChatProperties buildChatProperties() {
        ChatProperties.PreProcessorsProperties.BasicResourceManagerProperties basicResourceManagerProperties =
                new ChatProperties.PreProcessorsProperties.BasicResourceManagerProperties(0, "required", "into-prompt", "too-long");
        ChatProperties.PreProcessorsProperties preProcessorsProperties = new ChatProperties.PreProcessorsProperties(
                new ChatProperties.PreProcessorsProperties.DefaultSystemPromptProperties("default prompt"),
                new ChatProperties.PreProcessorsProperties.ResourceCategorizationProperties("categorizer prompt"),
                basicResourceManagerProperties);
        return new ChatProperties(1000, preProcessorsProperties);
    }

    @Test
    void defaultSystemPromptPreProcessor_shouldReturnDefaultSystemPromptPreProcessorInstance() {
        // Given & When
        ChatPreProcessor result = configuration.defaultSystemPromptPreProcessor(buildChatProperties());

        // Then
        assertThat(result).isInstanceOf(DefaultSystemPromptPreProcessor.class);
    }

    @Test
    void resourceCategorizationPreProcessor_shouldReturnResourceCategorizationPreProcessorInstance() {
        // Given & When
        ChatPreProcessor result = configuration.resourceCategorizationPreProcessor(llmModelService, objectMapper, buildChatProperties());

        // Then
        assertThat(result).isInstanceOf(ResourceCategorizationPreProcessor.class);
    }

    @Test
    void basicResourceManagerPreProcessor_shouldReturnBasicResourceManagerPreProcessorInstance() {
        // Given & When
        ChatPreProcessor result = configuration.basicResourceManagerPreProcessor(resourceContentService, buildChatProperties());

        // Then
        assertThat(result).isInstanceOf(BasicResourceManagerPreProcessor.class);
    }

    @Test
    void chatPreProcessingOrchestrator_shouldReturnOrchestratorWrappingProvidedProcessors() {
        // Given & When
        ChatPreProcessingOrchestrator result = configuration.chatPreProcessingOrchestrator(List.of(firstProcessor, secondProcessor));

        // Then
        assertThat(result).isInstanceOf(ChatPreProcessingOrchestratorImpl.class);
    }
}
