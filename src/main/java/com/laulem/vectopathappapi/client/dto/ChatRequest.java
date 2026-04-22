package com.laulem.vectopathappapi.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.laulem.vectopathappapi.business.model.SendChatMessageCommand;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class ChatRequest {

    @JsonProperty("message")
    @NotNull
    @NotEmpty
    private String message;

    @JsonProperty("resource_ids")
    private List<UUID> resourceIds;

    @JsonProperty("enabled_tools")
    private Set<String> enabledTools;

    @JsonProperty("llm_model")
    private String llmModel;

    @JsonIgnore
    public SendChatMessageCommand toBusinessRequest(UUID conversationId) {
        return new SendChatMessageCommand(conversationId, message, resourceIds, enabledTools, llmModel);
    }
}
