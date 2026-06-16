package com.laulem.vectopath.conversation.api.infra.service.factory;

import com.laulem.vectopath.conversation.api.infra.properties.LlmModelConfig;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;

public class OllamaChatClientFactory implements ChatClientFactory {
    private static final String PROVIDER_KEY = "ollama";

    @Override
    public boolean supports(String provider) {
        return PROVIDER_KEY.equalsIgnoreCase(provider);
    }

    @Override
    public ChatClient build(LlmModelConfig config) {
        OllamaApi.Builder apiBuilder = OllamaApi.builder();
        if (StringUtils.hasText(config.baseUrl())) {
            apiBuilder.baseUrl(config.baseUrl());
        }

        OllamaChatOptions.Builder optionsBuilder = OllamaChatOptions.builder()
                .model(config.model());
        applyOptions(optionsBuilder, config.options());

        OllamaChatModel chatModel = OllamaChatModel.builder()
                .ollamaApi(apiBuilder.build())
                .defaultOptions(optionsBuilder.build())
                .build();

        return ChatClient.builder(chatModel).build();
    }

    private void applyOptions(OllamaChatOptions.Builder builder, Map<String, Object> options) {
        if (options == null || options.isEmpty()) return;
        toDouble(options, "temperature").ifPresent(builder::temperature);
        toDouble(options, "topP").ifPresent(builder::topP);
        toInt(options, "maxTokens").ifPresent(builder::maxTokens);
        toDouble(options, "frequencyPenalty").ifPresent(builder::frequencyPenalty);
        toDouble(options, "presencePenalty").ifPresent(builder::presencePenalty);
        toInt(options, "topK").ifPresent(builder::topK);
    }

    private Optional<Double> toDouble(Map<String, Object> options, String key) {
        return Optional.ofNullable(options.get(key))
                .filter(Double.class::isInstance)
                .map(Double.class::cast);
    }

    private Optional<Integer> toInt(Map<String, Object> options, String key) {
        return Optional.ofNullable(options.get(key))
                .filter(Integer.class::isInstance)
                .map(Integer.class::cast);
    }
}


