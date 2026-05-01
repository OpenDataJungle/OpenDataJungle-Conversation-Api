package com.laulem.vectopathappapi.infra.model;

import com.laulem.vectopathappapi.business.model.SendChatMessageCommand;

public record ChatContext(
        SendChatMessageCommand command,
        String userMessage,
        String systemMessage,
        boolean includeSearchTool) {

    public static ChatContext of(SendChatMessageCommand command, String systemMessage) {
        return new ChatContext(command, command.message(), systemMessage, true);
    }

    public ChatContext withSystemMessage(String newSystemMessage) {
        return new ChatContext(command, userMessage, newSystemMessage, includeSearchTool);
    }

    public ChatContext withIncludeSearchTool(boolean newIncludeSearchTool) {
        return new ChatContext(command, userMessage, systemMessage, newIncludeSearchTool);
    }
}
