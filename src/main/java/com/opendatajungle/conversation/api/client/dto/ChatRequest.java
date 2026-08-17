package com.opendatajungle.conversation.api.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.opendatajungle.conversation.api.business.model.SendChatMessageCommand;
import com.opendatajungle.conversation.api.shared.validation.SizeType;
import com.opendatajungle.conversation.api.shared.validation.ValidSizeByType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record ChatRequest(
        @JsonProperty("message")
        @NotNull
        @NotEmpty
        @ValidSizeByType(SizeType.MESSAGE)
        String message,

        @JsonProperty("resource_ids")
        @ValidSizeByType(SizeType.RESOURCE_IDS)
        List<UUID> resourceIds,

        @JsonProperty("enabled_tools")
        @ValidSizeByType(SizeType.ENABLED_TOOLS)
        Set<String> enabledTools,

        @JsonProperty("llm_model") String llmModel) {

    @JsonIgnore
    public SendChatMessageCommand toBusinessRequest(UUID conversationId) {
        return new SendChatMessageCommand(conversationId, message, resourceIds, enabledTools, llmModel);
    }
}
