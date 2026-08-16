package com.opendatajungle.conversation.api.infra.tool;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class McpToolCallbackWrapperTest {

    @Mock
    private ToolCallback delegate;

    @Mock
    private ChatRequestHolder chatRequestHolder;

    @Mock
    private ToolDefinition toolDefinition;

    @Test
    void getToolDefinition_shouldDelegateToWrappedCallback() {
        // Given
        McpToolCallbackWrapper wrapper = new McpToolCallbackWrapper(delegate, chatRequestHolder);
        when(delegate.getToolDefinition()).thenReturn(toolDefinition);

        // When
        ToolDefinition result = wrapper.getToolDefinition();

        // Then
        assertThat(result).isEqualTo(toolDefinition);
    }

    @Test
    void call_shouldReturnDelegateResultAndRecordToolResult_whenResultIsNotNull() {
        // Given
        McpToolCallbackWrapper wrapper = new McpToolCallbackWrapper(delegate, chatRequestHolder);
        when(delegate.call("input")).thenReturn("output");
        when(delegate.getToolDefinition()).thenReturn(toolDefinition);
        when(toolDefinition.name()).thenReturn("tool-name");

        // When
        String result = wrapper.call("input");

        // Then
        assertThat(result).isEqualTo("output");
        verify(chatRequestHolder).addToolResult("tool-name", "input", Map.of("response", "output"));
    }

    @Test
    void call_shouldRecordEmptyResponse_whenDelegateReturnsNull() {
        // Given
        McpToolCallbackWrapper wrapper = new McpToolCallbackWrapper(delegate, chatRequestHolder);
        when(delegate.call("input")).thenReturn(null);
        when(delegate.getToolDefinition()).thenReturn(toolDefinition);
        when(toolDefinition.name()).thenReturn("tool-name");

        // When
        String result = wrapper.call("input");

        // Then
        assertThat(result).isNull();
        verify(chatRequestHolder).addToolResult("tool-name", "input", Map.of("response", ""));
    }
}
