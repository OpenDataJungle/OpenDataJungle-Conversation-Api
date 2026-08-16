package com.opendatajungle.conversation.api.infra.service.chatpreprocessor;

import com.opendatajungle.conversation.api.business.model.SendChatMessageCommand;
import com.opendatajungle.conversation.api.infra.model.ChatContext;
import com.opendatajungle.conversation.api.infra.model.ResourceContent;
import com.opendatajungle.conversation.api.infra.model.ResourceRoutingStrategy;
import com.opendatajungle.conversation.api.infra.properties.ChatProperties;
import com.opendatajungle.conversation.api.infra.service.ResourceContentService;
import com.opendatajungle.conversation.api.infra.tool.TransientContentMarker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BasicResourceManagerPreProcessorTest {

    @Mock
    private ResourceContentService resourceContentService;

    private BasicResourceManagerPreProcessor preProcessor;

    private static ChatProperties buildChatProperties(int maxFileContentsTokens, String requiredPrompt, String intoPrompt, String tooLongPrompt) {
        ChatProperties.PreProcessorsProperties.BasicResourceManagerProperties basicResourceManagerProperties =
                new ChatProperties.PreProcessorsProperties.BasicResourceManagerProperties(maxFileContentsTokens, requiredPrompt, intoPrompt, tooLongPrompt);
        ChatProperties.PreProcessorsProperties preProcessorsProperties = new ChatProperties.PreProcessorsProperties(
                new ChatProperties.PreProcessorsProperties.DefaultSystemPromptProperties("default prompt"),
                new ChatProperties.PreProcessorsProperties.ResourceCategorizationProperties("categorizer prompt"),
                basicResourceManagerProperties);
        return new ChatProperties(50000, preProcessorsProperties);
    }

    @BeforeEach
    void setUp() {
        preProcessor = new BasicResourceManagerPreProcessor(
                resourceContentService,
                buildChatProperties(1000, "required-prompt", "into-prompt", "too-long-prompt"));
    }

    private SendChatMessageCommand buildCommand(List<UUID> resourceIds) {
        return new SendChatMessageCommand(UUID.randomUUID(), "hello", resourceIds, null, "default");
    }

    @Test
    void getOrder_shouldReturnConfiguredOrder() {
        // Given & When & Then
        assertThat(preProcessor.getOrder()).isEqualTo(BasicResourceManagerPreProcessor.ORDER);
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
    void process_shouldReturnContextUnchanged_whenResourceIdsIsNull() {
        // Given
        ChatContext chatContext = ChatContext.of(buildCommand(null), "system");

        // When
        ChatContext result = preProcessor.process(chatContext);

        // Then
        assertThat(result).isEqualTo(chatContext);
    }

    @Test
    void process_shouldReturnContextUnchanged_whenStrategyIsNone() {
        // Given
        ChatContext chatContext = ChatContext.of(buildCommand(List.of(UUID.randomUUID())), "system");

        // When
        ChatContext result = preProcessor.process(chatContext);

        // Then
        assertThat(result).isEqualTo(chatContext);
    }

    @Test
    void process_shouldComposeSystemMessageWithRequiredPrompt_whenStrategyIsInternalSearchAndUserSystemMessagePresent() {
        // Given
        ChatContext chatContext = ChatContext.of(buildCommand(List.of(UUID.randomUUID())), "user system message")
                .withRoutingStrategy(ResourceRoutingStrategy.INTERNAL_SEARCH);

        // When
        ChatContext result = preProcessor.process(chatContext);

        // Then
        assertThat(result.systemMessage()).isEqualTo("required-prompt"
                + BasicResourceManagerPreProcessor.ADDITIONAL_CONTEXT_USER_DEFINED_LOWER_PRIORITY + "user system message");
    }

    @Test
    void process_shouldUseRequiredPromptOnly_whenStrategyIsInternalSearchAndUserSystemMessageBlank() {
        // Given
        ChatContext chatContext = ChatContext.of(buildCommand(List.of(UUID.randomUUID())), null)
                .withRoutingStrategy(ResourceRoutingStrategy.INTERNAL_SEARCH);

        // When
        ChatContext result = preProcessor.process(chatContext);

        // Then
        assertThat(result.systemMessage()).isEqualTo("required-prompt");
    }

    @Test
    void process_shouldReturnContextUnchanged_whenIncludeInPromptAndNoContentsFound() {
        // Given
        List<UUID> resourceIds = List.of(UUID.randomUUID());
        ChatContext chatContext = ChatContext.of(buildCommand(resourceIds), "system")
                .withRoutingStrategy(ResourceRoutingStrategy.INCLUDE_IN_PROMPT);
        when(resourceContentService.fetchContents(resourceIds)).thenReturn(List.of());

        // When
        ChatContext result = preProcessor.process(chatContext);

        // Then
        assertThat(result).isEqualTo(chatContext);
    }

    @Test
    void process_shouldIncludeResourcesInUserMessage_whenIncludeInPromptAndContentFitsBudget() {
        // Given
        UUID resourceId = UUID.randomUUID();
        List<UUID> resourceIds = List.of(resourceId);
        ChatContext chatContext = ChatContext.of(buildCommand(resourceIds), "system")
                .withRoutingStrategy(ResourceRoutingStrategy.INCLUDE_IN_PROMPT);
        ResourceContent resourceContent = new ResourceContent(resourceId, "file.txt", "small content");
        when(resourceContentService.fetchContents(resourceIds)).thenReturn(List.of(resourceContent));

        // When
        ChatContext result = preProcessor.process(chatContext);

        // Then
        assertThat(result.includeSearchTool()).isFalse();
        assertThat(result.userMessage()).contains("file.txt").contains("small content").contains(chatContext.userMessage());
        assertThat(TransientContentMarker.containsTransientContent(result.userMessage())).isTrue();
    }

    @Test
    void process_shouldReturnTooLongContext_whenIncludeInPromptAndContentExceedsBudget() {
        // Given
        BasicResourceManagerPreProcessor tightBudgetPreProcessor = new BasicResourceManagerPreProcessor(
                resourceContentService, buildChatProperties(1, "required-prompt", "into-prompt", "too-long-prompt"));
        UUID resourceId = UUID.randomUUID();
        List<UUID> resourceIds = List.of(resourceId);
        ChatContext chatContext = ChatContext.of(buildCommand(resourceIds), "system")
                .withRoutingStrategy(ResourceRoutingStrategy.INCLUDE_IN_PROMPT);
        ResourceContent resourceContent = new ResourceContent(resourceId, "file.txt", "a very long piece of file content that exceeds the tiny token budget");
        when(resourceContentService.fetchContents(resourceIds)).thenReturn(List.of(resourceContent));

        // When
        ChatContext result = tightBudgetPreProcessor.process(chatContext);

        // Then
        assertThat(result.systemMessage()).isEqualTo("too-long-prompt");
        assertThat(result.userMessage()).contains("too-long-prompt").contains(chatContext.userMessage());
        assertThat(TransientContentMarker.containsTransientContent(result.userMessage())).isTrue();
    }
}
