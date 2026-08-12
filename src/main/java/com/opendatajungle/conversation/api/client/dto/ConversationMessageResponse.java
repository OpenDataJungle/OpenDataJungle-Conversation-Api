package com.opendatajungle.conversation.api.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.opendatajungle.conversation.api.business.model.ConversationMessage;
import com.opendatajungle.conversation.api.business.model.ToolResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConversationMessageResponse(
        @JsonProperty("id") UUID id,
        @JsonProperty("conversation_id") UUID conversationId,
        /** Message type: USER | ASSISTANT | SYSTEM */
        @JsonProperty("type") String type,
        @JsonProperty("content") String content,
        @JsonProperty("created_at") LocalDateTime createdAt,
        @JsonProperty("tool_results") List<ToolResult> toolResults) {

    public ConversationMessageResponse(ConversationMessage message) {
        this(
                message.id(),
                message.conversationId(),
                message.type(),
                message.content(),
                message.createdAt(),
                message.toolResults()
        );
    }
}
