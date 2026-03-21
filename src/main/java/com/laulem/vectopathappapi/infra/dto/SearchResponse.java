package com.laulem.vectopathappapi.infra.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.laulem.vectopathappapi.business.model.Search;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchResponse {

    @JsonProperty("vector_id")
    private UUID vectorId;

    @JsonProperty("resource_id")
    private UUID resourceId;

    @JsonProperty("resource_name")
    private String resourceName;

    @JsonProperty("content")
    private String content;

    @JsonProperty("content_type")
    private String contentType;

    @JsonProperty("metadata")
    private String metadata;

    @JsonProperty("similarity_score")
    private Double similarityScore;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    public static Search toSearch(final SearchResponse searchResponse) {
        Search search = new Search();
        search.setVectorId(searchResponse.getVectorId());
        search.setResourceId(searchResponse.getResourceId());
        search.setResourceName(searchResponse.getResourceName());
        search.setContent(searchResponse.getContent());
        search.setContentType(searchResponse.getContentType());
        search.setMetadata(searchResponse.getMetadata());
        search.setSimilarityScore(searchResponse.getSimilarityScore());
        search.setCreatedAt(searchResponse.getCreatedAt());
        search.setUpdatedAt(searchResponse.getUpdatedAt());
        return search;
    }
}
