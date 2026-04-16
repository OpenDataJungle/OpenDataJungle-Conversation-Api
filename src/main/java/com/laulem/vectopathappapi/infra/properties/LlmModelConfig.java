package com.laulem.vectopathappapi.infra.properties;

import lombok.Data;

@Data
public class LlmModelConfig {
    private String provider;
    private String apiKey;
    private String baseUrl;
    private String model;
    private String name;
}
