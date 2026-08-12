package com.opendatajungle.conversation.api.infra.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.opendatajungle.conversation.api.infra.model.Search;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SearchResponse(
        @JsonProperty("vector_id") UUID vectorId,
        @JsonProperty("resource_id") UUID resourceId,
        @JsonProperty("resource_name") String resourceName,
        @JsonProperty("content") String content,
        @JsonProperty("content_type") String contentType,
        @JsonProperty("metadata") String metadata,
        @JsonProperty("similarity_score") Double similarityScore,
        @JsonProperty("created_at") LocalDateTime createdAt,
        @JsonProperty("updated_at") LocalDateTime updatedAt) {

    public static Search toSearch(final SearchResponse searchResponse) {
        return new Search(
                searchResponse.vectorId(),
                searchResponse.resourceId(),
                searchResponse.resourceName(),
                searchResponse.content(),
                searchResponse.contentType(),
                searchResponse.metadata(),
                searchResponse.similarityScore(),
                searchResponse.createdAt(),
                searchResponse.updatedAt()
        );
    }
}
