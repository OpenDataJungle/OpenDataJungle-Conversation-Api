package com.opendatajungle.conversation.api.infra.service;

import org.springframework.ai.tool.ToolCallback;

import java.util.Set;

public interface McpClientService {
    ToolCallback[] getRequiredToolCallbacks();

    ToolCallback[] getRequiredToolCallbacksWithAdditional(Set<String> enabledTools);
}
