package com.opendatajungle.conversation.api.client.dto;

import com.opendatajungle.conversation.api.business.model.ConversationMessage;
import com.opendatajungle.conversation.api.business.model.ToolResult;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ConversationMessageResponseTest {

    @Test
    void constructor_shouldMapAllFields_fromConversationMessage() {
        // Given
        UUID id = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();
        List<ToolResult> toolResults = List.of(new ToolResult("t1", "query", Map.of("k", "v")));
        ConversationMessage message = new ConversationMessage(id, conversationId, "ASSISTANT", "Hello", createdAt, toolResults);

        // When
        ConversationMessageResponse response = new ConversationMessageResponse(message);

        // Then
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.conversationId()).isEqualTo(conversationId);
        assertThat(response.type()).isEqualTo("ASSISTANT");
        assertThat(response.content()).isEqualTo("Hello");
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.toolResults()).isEqualTo(toolResults);
    }
}
