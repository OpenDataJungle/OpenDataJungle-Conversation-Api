package com.opendatajungle.conversation.api.infra.tool;

import com.opendatajungle.conversation.api.business.model.ToolResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChatRequestHolderTest {

    private final ChatRequestHolder chatRequestHolder = new ChatRequestHolder();

    @AfterEach
    void tearDown() {
        chatRequestHolder.clear();
    }

    @Test
    void getToolResults_shouldReturnEmptyList_whenNoneAdded() {
        // Given & When
        List<ToolResult> toolResults = chatRequestHolder.getToolResults();

        // Then
        assertThat(toolResults).isEmpty();
    }

    @Test
    void addToolResult_shouldAppendToolResult() {
        // Given
        Map<String, Object> result = Map.of("data", "value");

        // When
        chatRequestHolder.addToolResult("tool-id", "query", result);

        // Then
        List<ToolResult> toolResults = chatRequestHolder.getToolResults();
        assertThat(toolResults).hasSize(1);
        assertThat(toolResults.getFirst().id()).isEqualTo("tool-id");
        assertThat(toolResults.getFirst().query()).isEqualTo("query");
        assertThat(toolResults.getFirst().result()).isEqualTo(result);
    }

    @Test
    void setResourceIds_shouldMakeIdsRetrievableViaGetResourceIds() {
        // Given
        List<UUID> resourceIds = List.of(UUID.randomUUID());

        // When
        chatRequestHolder.setResourceIds(resourceIds);

        // Then
        assertThat(chatRequestHolder.getResourceIds()).isEqualTo(resourceIds);
    }

    @Test
    void getResourceIds_shouldReturnNull_whenNeverSet() {
        // Given & When
        List<UUID> resourceIds = chatRequestHolder.getResourceIds();

        // Then
        assertThat(resourceIds).isNull();
    }

    @Test
    void clear_shouldResetState() {
        // Given
        chatRequestHolder.setResourceIds(List.of(UUID.randomUUID()));
        chatRequestHolder.addToolResult("tool-id", "query", Map.of());

        // When
        chatRequestHolder.clear();

        // Then
        assertThat(chatRequestHolder.getResourceIds()).isNull();
        assertThat(chatRequestHolder.getToolResults()).isEmpty();
    }
}
