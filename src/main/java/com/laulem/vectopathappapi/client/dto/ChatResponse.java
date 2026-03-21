package com.laulem.vectopathappapi.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.laulem.vectopathappapi.business.model.ChatResult;
import com.laulem.vectopathappapi.business.model.ToolResult;

import java.util.List;

public class ChatResponse {

    @JsonProperty("reply")
    private final String reply;

    @JsonProperty("tool_results")
    private final List<ToolResult> toolResults;

    public ChatResponse(ChatResult result) {
        this.reply = result.reply();
        this.toolResults = result.toolResults();
    }

    public String getReply() {
        return reply;
    }

    public List<ToolResult> getToolResults() {
        return toolResults;
    }
}
