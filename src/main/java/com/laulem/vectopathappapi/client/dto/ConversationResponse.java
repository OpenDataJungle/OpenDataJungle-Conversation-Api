package com.laulem.vectopathappapi.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.laulem.vectopathappapi.business.model.Conversation;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConversationResponse(
        @JsonProperty("id") UUID id,
        @JsonProperty("user_id") String userId,
        @JsonProperty("title") String title,
        @JsonProperty("system_message") String systemMessage,
        @JsonProperty("created_at") LocalDateTime createdAt,
        @JsonProperty("last_message_at") LocalDateTime lastMessageAt) {

    public static ConversationResponse map(Conversation conversation) {
        return new ConversationResponse(
                conversation.getId(),
                conversation.getUserId(),
                conversation.getTitle(),
                conversation.getSystemMessage(),
                conversation.getCreatedAt(),
                conversation.getLastMessageAt()
        );
    }
}
