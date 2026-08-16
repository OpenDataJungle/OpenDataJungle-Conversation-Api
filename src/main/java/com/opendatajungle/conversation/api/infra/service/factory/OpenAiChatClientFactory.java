package com.opendatajungle.conversation.api.infra.service.factory;

import com.opendatajungle.conversation.api.infra.properties.LlmModelConfig;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
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
        OpenAiChatOptions.Builder optionsBuilder = OpenAiChatOptions.builder()
                .apiKey(config.apiKey())
                .model(config.model());
        if (StringUtils.hasText(config.baseUrl())) {
            optionsBuilder.baseUrl(config.baseUrl());
        }
        applyOptions(optionsBuilder, config.options());

        //optionsBuilder.reasoningEffort("none");
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .options(optionsBuilder.build())
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
        toString(options, "reasoningEffort").ifPresent(builder::reasoningEffort);
    }

    private Optional<Double> toDouble(Map<String, Object> options, String key) {
        return Optional.ofNullable(options.get(key))
                .filter(Double.class::isInstance)
                .map(Double.class::cast);
    }

    private Optional<String> toString(Map<String, Object> options, String key) {
        return Optional.ofNullable(options.get(key))
                .filter(String.class::isInstance)
                .map(String.class::cast);
    }

    private Optional<Integer> toInt(Map<String, Object> options, String key) {
        return Optional.ofNullable(options.get(key))
                .filter(Integer.class::isInstance)
                .map(Integer.class::cast);
    }
}
