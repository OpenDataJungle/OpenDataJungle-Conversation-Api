package com.laulem.vectopath.conversation.api.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.laulem.vectopath.conversation.api.business.model.ChatResult;
import com.laulem.vectopath.conversation.api.business.model.ToolResult;

import java.util.List;

public record ChatResponse(
        @JsonProperty("reply") String reply,
        @JsonProperty("tool_results") List<ToolResult> toolResults) {

    public ChatResponse(ChatResult result) {
        this(result.reply(), result.toolResults());
    }
}
