package com.laulem.vectopathappapi.business.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Conversation {
    private UUID id;
    private String userId;
    private String title;
    private String systemMessage;
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;

    public Conversation() {
    }

    public Conversation(UUID id, String userId, String title, String systemMessage, LocalDateTime createdAt, LocalDateTime lastMessageAt) {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }
}
