package com.laulem.vectopathappapi.infra.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laulem.vectopathappapi.infra.properties.LlmModelConfig;
import com.laulem.vectopathappapi.infra.properties.LlmProperties;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class LlmModelService {
    private final Map<String, LlmModelConfig> modelConfigs;
    private final Map<String, ChatClient> chatClients;

    public LlmModelService(LlmProperties llmProperties, ObjectMapper objectMapper) {
        try {
            Map<String, LlmModelConfig> parsed = objectMapper.readValue(llmProperties.getModelsJson(), new TypeReference<>() {
            });
            this.modelConfigs = Collections.unmodifiableMap(parsed);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse VECTO_PATH_LLM_MODELS JSON. Check the environment variable format.", e);
        }
        this.chatClients = Collections.unmodifiableMap(buildChatClients(modelConfigs));

        // Check the presence of defaultModel, throw an error if not
        if (!modelConfigs.containsKey(LlmModelKey.DEFAULT.getKey())) {
            throw new IllegalStateException("Missing required LLM model configuration for key: '" + LlmModelKey.DEFAULT.getKey() + "'.");
        }
    }

    public ChatClient getDefaultModel() {
        return getModel(LlmModelKey.DEFAULT);
    }

    public boolean hasModel(String modelId) {
        return modelConfigs.containsKey(modelId);
    }

    public ChatClient getModel(LlmModelKey key) {
        return hasModel(key.getKey()) ? chatClients.get(key.getKey()) : getDefaultModel();
    }

    public ChatClient getModel(String name) {
        return hasModel(name) ? chatClients.get(name) : getDefaultModel();
    }

    public Optional<LlmModelConfig> getModelConfig(LlmModelKey key) {
        return Optional.ofNullable(modelConfigs.get(key.getKey()));
    }

    public boolean hasSpeedModel() {
        return chatClients.containsKey(LlmModelKey.SPEED.getKey());
    }

    public boolean hasCategorizerModel() {
        return chatClients.containsKey(LlmModelKey.CATEGORIZER.getKey());
    }

    public boolean hasLongContextModel() {
        return chatClients.containsKey(LlmModelKey.LONG_CONTEXT.getKey());
    }

    private Map<String, ChatClient> buildChatClients(Map<String, LlmModelConfig> models) {
        Map<String, ChatClient> clients = new HashMap<>();
        for (Map.Entry<String, LlmModelConfig> entry : models.entrySet()) {
            clients.put(entry.getKey(), buildChatClient(entry.getValue()));
        }
        return clients;
    }

    private ChatClient buildChatClient(LlmModelConfig config) {
        return switch (config.getProvider().toLowerCase()) {
            case "openai" -> buildOpenAiChatClient(config);
            case "ollama" -> buildOllamaChatClient(config);
            default ->
                    throw new IllegalArgumentException("Unsupported LLM provider: '" + config.getProvider() + "'. Supported: openai, ollama.");
        };
    }

    private ChatClient buildOpenAiChatClient(LlmModelConfig config) {
        OpenAiApi.Builder apiBuilder = OpenAiApi.builder()
                .apiKey(config.getApiKey());
        if (StringUtils.hasText(config.getBaseUrl())) {
            apiBuilder.baseUrl(config.getBaseUrl());
        }
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(apiBuilder.build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(config.getModel())
                        .build())
                .build();
        return ChatClient.builder(chatModel).build();
    }

    private ChatClient buildOllamaChatClient(LlmModelConfig config) {
        OllamaApi.Builder apiBuilder = OllamaApi.builder();
        if (StringUtils.hasText(config.getBaseUrl())) {
            apiBuilder.baseUrl(config.getBaseUrl());
        }
        OllamaChatModel chatModel = OllamaChatModel.builder()
                .ollamaApi(apiBuilder.build())
                .defaultOptions(OllamaChatOptions.builder()
                        .model(config.getModel())
                        .build())
                .build();
        return ChatClient.builder(chatModel).build();
    }
}
