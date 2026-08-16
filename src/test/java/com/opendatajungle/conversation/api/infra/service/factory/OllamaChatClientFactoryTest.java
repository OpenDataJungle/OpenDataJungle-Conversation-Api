package com.opendatajungle.conversation.api.infra.service.factory;

import com.opendatajungle.conversation.api.infra.properties.LlmModelConfig;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OllamaChatClientFactoryTest {

    private final OllamaChatClientFactory factory = new OllamaChatClientFactory();

    @Test
    void supports_shouldReturnTrue_whenProviderIsOllamaIgnoringCase() {
        // Given & When & Then
        assertThat(factory.supports("ollama")).isTrue();
        assertThat(factory.supports("OLLAMA")).isTrue();
    }

    @Test
    void supports_shouldReturnFalse_whenProviderIsNotOllama() {
        // Given & When & Then
        assertThat(factory.supports("openai")).isFalse();
    }

    @Test
    void build_shouldReturnChatClient_withBaseUrlAndOptions() {
        // Given
        LlmModelConfig config = new LlmModelConfig(
                "ollama", null, "http://localhost:11434", "llama3", "default",
                Map.of("temperature", 0.5, "topP", 0.9, "maxTokens", 100,
                        "frequencyPenalty", 0.1, "presencePenalty", 0.2, "topK", 40));

        // When
        ChatClient chatClient = factory.build(config);

        // Then
        assertThat(chatClient).isNotNull();
    }

    @Test
    void build_shouldReturnChatClient_whenBaseUrlIsBlank() {
        // Given
        LlmModelConfig config = new LlmModelConfig("ollama", null, "", "llama3", "default", Map.of());

        // When
        ChatClient chatClient = factory.build(config);

        // Then
        assertThat(chatClient).isNotNull();
    }

    @Test
    void build_shouldReturnChatClient_whenOptionsIsNull() {
        // Given
        LlmModelConfig config = new LlmModelConfig("ollama", null, "http://localhost:11434", "llama3", "default", null);

        // When
        ChatClient chatClient = factory.build(config);

        // Then
        assertThat(chatClient).isNotNull();
    }

    @Test
    void build_shouldIgnoreNonMatchingOptionTypes() {
        // Given
        LlmModelConfig config = new LlmModelConfig(
                "ollama", null, "http://localhost:11434", "llama3", "default",
                Map.of("temperature", "not-a-double", "maxTokens", "not-an-int"));

        // When
        ChatClient chatClient = factory.build(config);

        // Then
        assertThat(chatClient).isNotNull();
    }
}
