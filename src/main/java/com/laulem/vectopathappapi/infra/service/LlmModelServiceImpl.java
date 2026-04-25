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

@Service
public class LlmModelServiceImpl implements LlmModelService {
    private final Map<String, LlmModelConfig> modelConfigs;
    private final Map<String, ChatClient> chatClients;

    public LlmModelServiceImpl(LlmProperties llmProperties, ObjectMapper objectMapper) {
        try {
            Map<String, LlmModelConfig> parsed = objectMapper.readValue(llmProperties.modelsJson(), new TypeReference<>() {
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

    @Override
    public ChatClient getDefaultModel() {
        return getModel(LlmModelKey.DEFAULT);
    }

    @Override
    public boolean hasModel(String modelId) {
        return modelConfigs.containsKey(modelId);
    }

    @Override
    public ChatClient getModel(LlmModelKey key) {
        return hasModel(key.getKey()) ? chatClients.get(key.getKey()) : getDefaultModel();
    }

    @Override
    public ChatClient getModel(String name) {
        if (!StringUtils.hasText(name)) {
            return getDefaultModel();
        }
        return hasModel(name) ? chatClients.get(name) : getDefaultModel();
    }

    @Override
    public boolean hasSpeedModel() {
        return chatClients.containsKey(LlmModelKey.SPEED.getKey());
    }

    @Override
    public boolean hasCategorizerModel() {
        return chatClients.containsKey(LlmModelKey.CATEGORIZER.getKey());
    }

    @Override
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
        return switch (config.provider().toLowerCase()) {
            case "openai" -> buildOpenAiChatClient(config);
            case "ollama" -> buildOllamaChatClient(config);
            default ->
                    throw new IllegalArgumentException("Unsupported LLM provider: '" + config.provider() + "'. Supported: openai, ollama.");
        };
    }

    private ChatClient buildOpenAiChatClient(LlmModelConfig config) {
        OpenAiApi.Builder apiBuilder = OpenAiApi.builder()
                .apiKey(config.apiKey());
        if (StringUtils.hasText(config.baseUrl())) {
            apiBuilder.baseUrl(config.baseUrl());
        }
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(apiBuilder.build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(config.model())
                        .build())
                .build();
        return ChatClient.builder(chatModel).build();
    }

    private ChatClient buildOllamaChatClient(LlmModelConfig config) {
        OllamaApi.Builder apiBuilder = OllamaApi.builder();
        if (StringUtils.hasText(config.baseUrl())) {
            apiBuilder.baseUrl(config.baseUrl());
        }
        OllamaChatModel chatModel = OllamaChatModel.builder()
                .ollamaApi(apiBuilder.build())
                .defaultOptions(OllamaChatOptions.builder()
                        .model(config.model())
                        .maxTokens(100) // TODO
                        .build())
                .build();
        return ChatClient.builder(chatModel).build();
    }
}
