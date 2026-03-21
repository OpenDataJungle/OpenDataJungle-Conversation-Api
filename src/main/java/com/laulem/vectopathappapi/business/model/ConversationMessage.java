package com.laulem.vectopathappapi.business.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ConversationMessage {
    private UUID id;
    private UUID conversationId;
    /**
     * Message type: USER | ASSISTANT | SYSTEM
     */
    private String type;
    private String content;
    private LocalDateTime createdAt;
    private List<ToolResult> toolResults;

    public ConversationMessage(UUID id, UUID conversationId, String type, String content, LocalDateTime createdAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.type = type;
        this.content = content;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getConversationId() {
        return conversationId;
    }

    public void setConversationId(UUID conversationId) {
        this.conversationId = conversationId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<ToolResult> getToolResults() {
        return toolResults;
    }

    public void setToolResults(List<ToolResult> toolResults) {
        this.toolResults = toolResults;
    }
}
