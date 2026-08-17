package com.opendatajungle.conversation.api.infra.model;

import com.opendatajungle.conversation.api.business.model.SendChatMessageCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.opendatajungle.conversation.api.infra.tool.TransientContentMarker.strip;

public record ChatContext(
        SendChatMessageCommand command,
        String userMessage,
        String systemMessage,
        boolean includeSearchTool,
        String resourceRoutingStrategy,
        Map<String, Object> additionalData) {

    public ChatContext(SendChatMessageCommand command, String userMessage, String systemMessage, boolean includeSearchTool, String resourceRoutingStrategy, Map<String, Object> additionalData) {
        this.command = command;
        this.userMessage = userMessage;
        this.systemMessage = systemMessage;
        this.includeSearchTool = includeSearchTool;
        this.resourceRoutingStrategy = Objects.requireNonNullElse(resourceRoutingStrategy, ResourceRoutingStrategy.NONE);
        this.additionalData = Objects.requireNonNullElseGet(additionalData, HashMap::new);
    }

    public static ChatContext of(SendChatMessageCommand command, String systemMessage) {
        return new ChatContext(command, strip(command.message()), strip(systemMessage), true, null, null);
    }

    public ChatContext withSystemMessage(String newSystemMessage) {
        return new ChatContext(command, userMessage, newSystemMessage, includeSearchTool, resourceRoutingStrategy, additionalData);
    }

    public ChatContext withUserMessage(String newUserMessage) {
        return new ChatContext(command, newUserMessage, systemMessage, includeSearchTool, resourceRoutingStrategy, additionalData);
    }

    public ChatContext withIncludeSearchTool(boolean newIncludeSearchTool) {
        return new ChatContext(command, userMessage, systemMessage, newIncludeSearchTool, resourceRoutingStrategy, additionalData);
    }

    public ChatContext withRoutingStrategy(String newRoutingStrategy) {
        return new ChatContext(command, userMessage, systemMessage, includeSearchTool, newRoutingStrategy, additionalData);
    }
}
