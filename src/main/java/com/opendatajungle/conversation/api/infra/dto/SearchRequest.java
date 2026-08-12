package com.opendatajungle.conversation.api.infra.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SearchRequest(
        @JsonProperty("query") String query,
        @JsonProperty("limit") Integer limit,
        @JsonProperty("min_similarity") Double minSimilarity,
        @JsonProperty("resource_ids") List<UUID> resourceIds) {
    public SearchRequest {
        if (limit == null || limit <= 0) limit = 10;
        if (minSimilarity == null || minSimilarity < 0 || minSimilarity > 1) minSimilarity = 0.0;
    }
}
