package com.laulem.vectopath.conversation.api.infra.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laulem.vectopath.conversation.api.infra.properties.LlmModelConfig;
import com.laulem.vectopath.conversation.api.infra.properties.LlmProperties;
import com.laulem.vectopath.conversation.api.infra.service.factory.ChatClientFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LlmModelServiceImpl implements LlmModelService {
    private final List<ChatClientFactory> chatClientFactories;
    private final Map<String, LlmModelConfig> modelConfigs;
    private final Map<String, ChatClient> chatClients;

    public LlmModelServiceImpl(LlmProperties llmProperties, ObjectMapper objectMapper, List<ChatClientFactory> chatClientFactories) {
        this.chatClientFactories = chatClientFactories;
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
        return chatClientFactories.stream()
                .filter(factory -> factory.supports(config.provider()))
                .findFirst()
                .map(factory -> factory.build(config))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported LLM provider: '" + config.provider() + "'."
                ));
    }
}
