package com.laulem.vectopathappapi.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.laulem.vectopathappapi.business.model.SendChatMessageCommand;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record ChatRequest(
        @JsonProperty("message") @NotNull @NotEmpty String message,
        @JsonProperty("resource_ids") List<UUID> resourceIds,
        @JsonProperty("enabled_tools") Set<String> enabledTools,
        @JsonProperty("llm_model") String llmModel) {

    @JsonIgnore
    public SendChatMessageCommand toBusinessRequest(UUID conversationId) {
        return new SendChatMessageCommand(conversationId, message, resourceIds, enabledTools, llmModel);
    }
}
