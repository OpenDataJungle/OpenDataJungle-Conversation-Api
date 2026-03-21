package com.laulem.vectopathappapi.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.laulem.vectopathappapi.business.model.ConversationMessage;
import com.laulem.vectopathappapi.business.model.ToolResult;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConversationMessageResponse {
    @JsonProperty("id")
    private UUID id;

    @JsonProperty("conversation_id")
    private UUID conversationId;

    /**
     * Message type: USER | ASSISTANT | SYSTEM
     */
    @JsonProperty("type")
    private String type;

    @JsonProperty("content")
    private String content;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("tool_results")
    private List<ToolResult> toolResults;

    public static ConversationMessageResponse from(ConversationMessage message) {
        return new ConversationMessageResponse(
                message.getId(),
                message.getConversationId(),
                message.getType(),
                message.getContent(),
                message.getCreatedAt(),
                message.getToolResults()
        );
    }
}
