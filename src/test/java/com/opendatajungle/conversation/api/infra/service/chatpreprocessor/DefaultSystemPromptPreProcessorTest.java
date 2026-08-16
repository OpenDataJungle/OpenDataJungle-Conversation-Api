package com.opendatajungle.conversation.api.infra.service.chatpreprocessor;

import com.opendatajungle.conversation.api.business.model.SendChatMessageCommand;
import com.opendatajungle.conversation.api.infra.model.ChatContext;
import com.opendatajungle.conversation.api.infra.properties.ChatProperties;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultSystemPromptPreProcessorTest {

    private ChatProperties buildChatProperties(String basePrompt) {
        ChatProperties.PreProcessorsProperties.BasicResourceManagerProperties basicResourceManagerProperties =
                new ChatProperties.PreProcessorsProperties.BasicResourceManagerProperties(0, "required", "into-prompt", "too-long");
        ChatProperties.PreProcessorsProperties preProcessorsProperties = new ChatProperties.PreProcessorsProperties(
                new ChatProperties.PreProcessorsProperties.DefaultSystemPromptProperties(basePrompt),
                new ChatProperties.PreProcessorsProperties.ResourceCategorizationProperties("categorizer prompt"),
                basicResourceManagerProperties);
        return new ChatProperties(1000, preProcessorsProperties);
    }

    private final DefaultSystemPromptPreProcessor preProcessor = new DefaultSystemPromptPreProcessor(buildChatProperties("base prompt"));

    private final SendChatMessageCommand command = new SendChatMessageCommand(
            UUID.randomUUID(), "hello", null, null, "default");

    @Test
    void getOrder_shouldReturnConfiguredOrder() {
        // Given & When & Then
        assertThat(preProcessor.getOrder()).isEqualTo(DefaultSystemPromptPreProcessor.ORDER);
    }

    @Test
    void process_shouldUseBasePrompt_whenUserSystemMessageIsBlank() {
        // Given
        ChatContext chatContext = ChatContext.of(command, null);

        // When
        ChatContext result = preProcessor.process(chatContext);

        // Then
        assertThat(result.systemMessage()).isEqualTo("base prompt");
    }

    @Test
    void process_shouldUseBasePrompt_whenUserSystemMessageIsBlankString() {
        // Given
        ChatContext chatContext = ChatContext.of(command, "   ");

        // When
        ChatContext result = preProcessor.process(chatContext);

        // Then
        assertThat(result.systemMessage()).isEqualTo("base prompt");
    }

    @Test
    void process_shouldAppendUserSystemMessage_whenUserSystemMessagePresent() {
        // Given
        ChatContext chatContext = ChatContext.of(command, "user system message");

        // When
        ChatContext result = preProcessor.process(chatContext);

        // Then
        assertThat(result.systemMessage()).isEqualTo("base prompt" + DefaultSystemPromptPreProcessor.ADDITIONAL_CONTEXT_USER_DEFINED_LOWER_PRIORITY + "user system message");
    }
}
