package com.laulem.vectopathappapi.infra.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchRequest {

    @JsonProperty("query")
    private String query;

    @JsonProperty("limit")
    private int limit = 10;

    @JsonProperty("min_similarity")
    private double minSimilarity = 0.0;
}
