package com.laulem.vectopathappapi.infra.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchRequest {

    @JsonProperty("query")
    private String query;

    @JsonProperty("limit")
    private int limit = 10;

    @JsonProperty("min_similarity")
    private double minSimilarity = 0.0;

    @JsonProperty("resource_ids")
    private List<UUID> resourceIds;
}
