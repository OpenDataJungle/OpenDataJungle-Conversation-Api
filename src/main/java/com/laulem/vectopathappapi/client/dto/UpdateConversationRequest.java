package com.laulem.vectopathappapi.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateConversationRequest {

    @JsonProperty("title")
    private String title;

    @JsonProperty("system_message")
    private String systemMessage;

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
}

