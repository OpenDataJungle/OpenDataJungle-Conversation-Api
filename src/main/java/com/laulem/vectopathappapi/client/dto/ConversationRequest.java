package com.laulem.vectopathappapi.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ConversationRequest(
        @JsonProperty("title") String title,
        @JsonProperty("system_message") String systemMessage) {
}
