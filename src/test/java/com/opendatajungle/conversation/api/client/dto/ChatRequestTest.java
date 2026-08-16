package com.opendatajungle.conversation.api.client.dto;

import com.opendatajungle.conversation.api.business.model.SendChatMessageCommand;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ChatRequestTest {

    @Test
    void toBusinessRequest_shouldMapAllFieldsWithConversationId() {
        // Given
        UUID conversationId = UUID.randomUUID();
        List<UUID> resourceIds = List.of(UUID.randomUUID());
        Set<String> enabledTools = Set.of("search");
        ChatRequest request = new ChatRequest("Hello", resourceIds, enabledTools, "gpt-4");

        // When
        SendChatMessageCommand command = request.toBusinessRequest(conversationId);

        // Then
        assertThat(command).isEqualTo(new SendChatMessageCommand(conversationId, "Hello", resourceIds, enabledTools, "gpt-4"));
    }

    @Test
    void toBusinessRequest_shouldMapNullOptionalFields() {
        // Given
        UUID conversationId = UUID.randomUUID();
        ChatRequest request = new ChatRequest("Hello", null, null, null);

        // When
        SendChatMessageCommand command = request.toBusinessRequest(conversationId);

        // Then
        assertThat(command).isEqualTo(new SendChatMessageCommand(conversationId, "Hello", null, null, null));
    }
}
