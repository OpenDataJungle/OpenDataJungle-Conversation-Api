package com.opendatajungle.conversation.api.infra.service.factory;

import com.opendatajungle.conversation.api.infra.properties.LlmModelConfig;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;

public class OpenAiChatClientFactory implements ChatClientFactory {
    private static final String PROVIDER_KEY = "openai";

    @Override
    public boolean supports(String provider) {
        return PROVIDER_KEY.equalsIgnoreCase(provider);
    }

    @Override
    public ChatClient build(LlmModelConfig config) {
        OpenAiApi.Builder apiBuilder = OpenAiApi.builder()
                .apiKey(config.apiKey());
        if (StringUtils.hasText(config.baseUrl())) {
            apiBuilder.baseUrl(config.baseUrl());
        }

        OpenAiChatOptions.Builder optionsBuilder = OpenAiChatOptions.builder()
                .model(config.model());
        applyOptions(optionsBuilder, config.options());

        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(apiBuilder.build())
                .defaultOptions(optionsBuilder.build())
                .build();

        return ChatClient.builder(chatModel).build();
    }

    private void applyOptions(OpenAiChatOptions.Builder builder, Map<String, Object> options) {
        if (options == null || options.isEmpty()) return;
        toDouble(options, "temperature").ifPresent(builder::temperature);
        toDouble(options, "topP").ifPresent(builder::topP);
        toInt(options, "maxTokens").ifPresent(builder::maxTokens);
        toDouble(options, "frequencyPenalty").ifPresent(builder::frequencyPenalty);
        toDouble(options, "presencePenalty").ifPresent(builder::presencePenalty);
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
