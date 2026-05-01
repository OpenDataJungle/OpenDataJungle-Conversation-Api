package com.laulem.vectopathappapi.infra.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ResourceRoutingDecision(
        @JsonProperty("strategy") ResourceRoutingStrategy strategy) {

    @JsonCreator
    public ResourceRoutingDecision {
        if (strategy == null) strategy = ResourceRoutingStrategy.SEARCH;
    }

    public static ResourceRoutingDecision search() {
        return new ResourceRoutingDecision(ResourceRoutingStrategy.SEARCH);
    }

    public static ResourceRoutingDecision includeInPrompt() {
        return new ResourceRoutingDecision(ResourceRoutingStrategy.INCLUDE_IN_PROMPT);
    }
}
