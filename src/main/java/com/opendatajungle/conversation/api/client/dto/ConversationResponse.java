package com.opendatajungle.conversation.api.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.opendatajungle.conversation.api.business.model.Conversation;

import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConversationResponse(
        @JsonProperty("id") UUID id,
        @JsonProperty("user_id") String userId,
        @JsonProperty("title") String title,
        @JsonProperty("system_message") String systemMessage,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("last_message_at") Instant lastMessageAt) {

    public ConversationResponse(Conversation conversation) {
        this(
                conversation.getId(),
                conversation.getUserId(),
                conversation.getTitle(),
                conversation.getSystemMessage(),
                conversation.getCreatedAt(),
                conversation.getLastMessageAt()
        );
    }
}
