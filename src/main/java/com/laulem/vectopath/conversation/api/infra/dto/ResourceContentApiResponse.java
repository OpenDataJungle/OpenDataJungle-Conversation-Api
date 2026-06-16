package com.laulem.vectopath.conversation.api.infra.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.laulem.vectopath.conversation.api.infra.model.ResourceContent;

import java.util.UUID;

public record ResourceContentApiResponse(
        @JsonProperty("id") UUID id,
        @JsonProperty("name") String name,
        @JsonProperty("content") String content) {

    public ResourceContent toResourceContent() {
        return new ResourceContent(id, name, content);
    }
}
