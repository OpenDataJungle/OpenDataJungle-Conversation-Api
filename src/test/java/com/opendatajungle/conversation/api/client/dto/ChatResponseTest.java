package com.opendatajungle.conversation.api.client.dto;

import com.opendatajungle.conversation.api.business.model.ChatResult;
import com.opendatajungle.conversation.api.business.model.ToolResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ChatResponseTest {

    @Test
    void constructor_shouldMapReplyAndToolResults_fromChatResult() {
        // Given
        List<ToolResult> toolResults = List.of(new ToolResult("t1", "query", Map.of("k", "v")));
        ChatResult result = new ChatResult("Hi, how can I help?", toolResults);

        // When
        ChatResponse response = new ChatResponse(result);

        // Then
        assertThat(response.reply()).isEqualTo("Hi, how can I help?");
        assertThat(response.toolResults()).isEqualTo(toolResults);
    }

    @Test
    void constructor_shouldMapNullToolResultsAndReply_fromChatResult() {
        // Given
        ChatResult result = new ChatResult(null, null);

        // When
        ChatResponse response = new ChatResponse(result);

        // Then
        assertThat(response.reply()).isNull();
        assertThat(response.toolResults()).isNull();
    }
}
