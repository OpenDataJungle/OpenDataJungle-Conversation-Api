package com.laulem.vectopathappapi.infra.tool;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

import java.util.Map;

/**
 * Wrapper for ToolCallback to capture tool results and store them in ChatRequestHolder for later use in the conversation context.
 */
public class McpToolCallbackWrapper implements ToolCallback {

    private final ToolCallback delegate;
    private final ChatRequestHolder chatRequestHolder;

    public McpToolCallbackWrapper(ToolCallback delegate, ChatRequestHolder chatRequestHolder) {
        this.delegate = delegate;
        this.chatRequestHolder = chatRequestHolder;
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return delegate.getToolDefinition();
    }

    @Override
    public String call(String toolInput) {
        String result = delegate.call(toolInput);
        chatRequestHolder.addToolResult(delegate.getToolDefinition().name(), toolInput, Map.of("response", result != null ? result : ""));
        return result;
    }
}

