package com.opendatajungle.conversation.api.infra.model;

import com.opendatajungle.conversation.api.business.model.SendChatMessageCommand;
import com.opendatajungle.conversation.api.infra.tool.TransientContentMarker;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ChatContextTest {

    private final SendChatMessageCommand command = new SendChatMessageCommand(
            UUID.randomUUID(), "hello", null, null, "default");

    @Test
    void constructor_shouldDefaultRoutingStrategyToNone_whenNull() {
        // Given & When
        ChatContext chatContext = new ChatContext(command, "user", "system", true, null, null);

        // Then
        assertThat(chatContext.resourceRoutingStrategy()).isEqualTo(ResourceRoutingStrategy.NONE);
    }

    @Test
    void constructor_shouldDefaultAdditionalDataToEmptyMap_whenNull() {
        // Given & When
        ChatContext chatContext = new ChatContext(command, "user", "system", true, null, null);

        // Then
        assertThat(chatContext.additionalData()).isNotNull().isEmpty();
    }

    @Test
    void constructor_shouldKeepProvidedRoutingStrategyAndAdditionalData_whenProvided() {
        // Given
        Map<String, Object> additionalData = new HashMap<>(Map.of("key", "value"));

        // When
        ChatContext chatContext = new ChatContext(command, "user", "system", true, ResourceRoutingStrategy.INTERNAL_SEARCH, additionalData);

        // Then
        assertThat(chatContext.resourceRoutingStrategy()).isEqualTo(ResourceRoutingStrategy.INTERNAL_SEARCH);
        assertThat(chatContext.additionalData()).isEqualTo(additionalData);
    }

    @Test
    void of_shouldBuildContextFromCommand_withSearchToolEnabledAndNoneStrategy() {
        // Given & When
        ChatContext chatContext = ChatContext.of(command, "system message");

        // Then
        assertThat(chatContext.command()).isEqualTo(command);
        assertThat(chatContext.userMessage()).isEqualTo(command.message());
        assertThat(chatContext.systemMessage()).isEqualTo("system message");
        assertThat(chatContext.includeSearchTool()).isTrue();
        assertThat(chatContext.resourceRoutingStrategy()).isEqualTo(ResourceRoutingStrategy.NONE);
        assertThat(chatContext.additionalData()).isEmpty();
    }

    @Test
    void of_shouldStripTransientMarkers_whenUserMessageContainsThem() {
        // Given
        SendChatMessageCommand craftedCommand = new SendChatMessageCommand(
                UUID.randomUUID(),
                TransientContentMarker.START + "hidden" + TransientContentMarker.END + "visible",
                null, null, "default");

        // When
        ChatContext chatContext = ChatContext.of(craftedCommand, "system message");

        // Then
        assertThat(chatContext.userMessage()).isEqualTo("visible");
    }

    @Test
    void withSystemMessage_shouldReturnNewContextWithUpdatedSystemMessage_andPreserveOtherFields() {
        // Given
        ChatContext chatContext = ChatContext.of(command, "system message");

        // When
        ChatContext updated = chatContext.withSystemMessage("new system message");

        // Then
        assertThat(updated.systemMessage()).isEqualTo("new system message");
        assertThat(updated.userMessage()).isEqualTo(chatContext.userMessage());
        assertThat(updated.command()).isEqualTo(chatContext.command());
        assertThat(updated.includeSearchTool()).isEqualTo(chatContext.includeSearchTool());
        assertThat(updated.resourceRoutingStrategy()).isEqualTo(chatContext.resourceRoutingStrategy());
        assertThat(updated.additionalData()).isEqualTo(chatContext.additionalData());
    }

    @Test
    void withUserMessage_shouldReturnNewContextWithUpdatedUserMessage_andPreserveOtherFields() {
        // Given
        ChatContext chatContext = ChatContext.of(command, "system message");

        // When
        ChatContext updated = chatContext.withUserMessage("new user message");

        // Then
        assertThat(updated.userMessage()).isEqualTo("new user message");
        assertThat(updated.systemMessage()).isEqualTo(chatContext.systemMessage());
    }

    @Test
    void withIncludeSearchTool_shouldReturnNewContextWithUpdatedFlag_andPreserveOtherFields() {
        // Given
        ChatContext chatContext = ChatContext.of(command, "system message");

        // When
        ChatContext updated = chatContext.withIncludeSearchTool(false);

        // Then
        assertThat(updated.includeSearchTool()).isFalse();
        assertThat(updated.userMessage()).isEqualTo(chatContext.userMessage());
    }

    @Test
    void withRoutingStrategy_shouldReturnNewContextWithUpdatedStrategy_andPreserveOtherFields() {
        // Given
        ChatContext chatContext = ChatContext.of(command, "system message");

        // When
        ChatContext updated = chatContext.withRoutingStrategy(ResourceRoutingStrategy.INCLUDE_IN_PROMPT);

        // Then
        assertThat(updated.resourceRoutingStrategy()).isEqualTo(ResourceRoutingStrategy.INCLUDE_IN_PROMPT);
        assertThat(updated.userMessage()).isEqualTo(chatContext.userMessage());
    }
}
