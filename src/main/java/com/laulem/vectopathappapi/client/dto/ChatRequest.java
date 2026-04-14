package com.laulem.vectopathappapi.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
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
}
