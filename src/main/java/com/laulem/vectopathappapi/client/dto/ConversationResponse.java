package com.laulem.vectopathappapi.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.laulem.vectopathappapi.business.model.Conversation;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConversationResponse {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("system_message")
    private String systemMessage;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("last_message_at")
    private LocalDateTime lastMessageAt;

    public static ConversationResponse mapFrom(Conversation conversation) {
        ConversationResponse response = new ConversationResponse();
        response.id = conversation.getId();
        response.userId = conversation.getUserId();
        response.title = conversation.getTitle();
        response.systemMessage = conversation.getSystemMessage();
        response.createdAt = conversation.getCreatedAt();
        response.lastMessageAt = conversation.getLastMessageAt();
        return response;
    }
}



