package com.laulem.vectopathappapi.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.laulem.vectopathappapi.business.model.ChatResult;
import com.laulem.vectopathappapi.business.model.ToolResult;

import java.util.List;

public record ChatResponse(
        @JsonProperty("reply") String reply,
        @JsonProperty("tool_results") List<ToolResult> toolResults) {

    public ChatResponse(ChatResult result) {
        this(result.reply(), result.toolResults());
    }
}
