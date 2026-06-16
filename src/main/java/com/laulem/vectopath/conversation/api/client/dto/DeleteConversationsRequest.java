package com.laulem.vectopath.conversation.api.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record DeleteConversationsRequest(
        @JsonProperty("ids")
        @NotNull
        @NotEmpty(message = "Id list cannot be empty")
        List<UUID> ids) {
}
