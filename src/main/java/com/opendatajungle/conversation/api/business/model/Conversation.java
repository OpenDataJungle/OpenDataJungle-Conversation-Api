package com.opendatajungle.conversation.api.business.model;

import java.time.Instant;
import java.util.UUID;

public class Conversation {
    private UUID id;
    private String userId;
    private String title;
    private String systemMessage;
    private Instant createdAt;
    private Instant lastMessageAt;

    public Conversation() {
    }

    public Conversation(UUID id, String userId, String title, String systemMessage, Instant createdAt, Instant lastMessageAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.systemMessage = systemMessage;
        this.createdAt = createdAt;
        this.lastMessageAt = lastMessageAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSystemMessage() {
        return systemMessage;
    }

    public void setSystemMessage(String systemMessage) {
        this.systemMessage = systemMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(Instant lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }
}
