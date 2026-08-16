package com.opendatajungle.conversation.api.infra.service.factory;

import com.opendatajungle.conversation.api.infra.properties.LlmModelConfig;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OpenAiChatClientFactoryTest {

    private final OpenAiChatClientFactory factory = new OpenAiChatClientFactory();

    @Test
    void supports_shouldReturnTrue_whenProviderIsOpenAiIgnoringCase() {
        // Given & When & Then
        assertThat(factory.supports("openai")).isTrue();
        assertThat(factory.supports("OPENAI")).isTrue();
    }

    @Test
    void supports_shouldReturnFalse_whenProviderIsNotOpenAi() {
        // Given & When & Then
        assertThat(factory.supports("ollama")).isFalse();
    }

    @Test
    void build_shouldReturnChatClient_withBaseUrlAndOptions() {
        // Given
        LlmModelConfig config = new LlmModelConfig(
                "openai", "sk-test", "https://api.openai.com", "gpt-4o", "default",
                Map.of("temperature", 0.5, "topP", 0.9, "maxTokens", 100,
                        "frequencyPenalty", 0.1, "presencePenalty", 0.2));

        // When
        ChatClient chatClient = factory.build(config);

        // Then
        assertThat(chatClient).isNotNull();
    }

    @Test
    void build_shouldReturnChatClient_whenBaseUrlIsBlank() {
        // Given
        LlmModelConfig config = new LlmModelConfig("openai", "sk-test", "", "gpt-4o", "default", Map.of());

        // When
        ChatClient chatClient = factory.build(config);

        // Then
        assertThat(chatClient).isNotNull();
    }

    @Test
    void build_shouldReturnChatClient_whenOptionsIsNull() {
        // Given
        LlmModelConfig config = new LlmModelConfig("openai", "sk-test", "https://api.openai.com", "gpt-4o", "default", null);

        // When
        ChatClient chatClient = factory.build(config);

        // Then
        assertThat(chatClient).isNotNull();
    }

    @Test
    void build_shouldIgnoreNonMatchingOptionTypes() {
        // Given
        LlmModelConfig config = new LlmModelConfig(
                "openai", "sk-test", "https://api.openai.com", "gpt-4o", "default",
                Map.of("temperature", "not-a-double", "maxTokens", "not-an-int"));

        // When
        ChatClient chatClient = factory.build(config);

        // Then
        assertThat(chatClient).isNotNull();
    }
}
