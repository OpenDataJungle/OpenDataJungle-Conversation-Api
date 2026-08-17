package com.opendatajungle.conversation.api.infra.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opendatajungle.commons.business.exception.ParamException;
import com.opendatajungle.conversation.api.infra.properties.LlmModelConfig;
import com.opendatajungle.conversation.api.infra.properties.LlmProperties;
import com.opendatajungle.conversation.api.infra.service.factory.ChatClientFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LlmModelServiceImplTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    private ChatClientFactory chatClientFactory;
    @Mock
    private ChatClient defaultChatClient;
    @Mock
    private ChatClient speedChatClient;

    private String modelsJson(String... entries) {
        return "{" + String.join(",", entries) + "}";
    }

    @Test
    void constructor_shouldThrowIllegalStateException_whenModelsJsonIsMalformed() {
        // Given
        LlmProperties llmProperties = new LlmProperties("not-json");

        // When & Then
        assertThatThrownBy(() -> new LlmModelServiceImpl(llmProperties, objectMapper, List.of(chatClientFactory)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to parse OPEN_DATA_JUNGLE_LLM_MODELS JSON");
    }

    @Test
    void constructor_shouldThrowIllegalStateException_whenDefaultModelMissing() {
        // Given
        LlmProperties llmProperties = new LlmProperties(modelsJson(
                "\"speed\":{\"provider\":\"openai\",\"apiKey\":\"key\",\"baseUrl\":null,\"model\":\"gpt\",\"name\":\"speed\",\"options\":{}}"));

        // When & Then
        assertThatThrownBy(() -> new LlmModelServiceImpl(llmProperties, objectMapper, List.of(chatClientFactory)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Missing required LLM model configuration for key: 'default'");
    }

    @Test
    void constructor_shouldThrowIllegalArgumentException_whenProviderUnsupported() {
        // Given
        LlmProperties llmProperties = new LlmProperties(modelsJson(
                "\"default\":{\"provider\":\"unsupported\",\"apiKey\":\"key\",\"baseUrl\":null,\"model\":\"gpt\",\"name\":\"default\",\"options\":{}}"));
        when(chatClientFactory.supports("unsupported")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> new LlmModelServiceImpl(llmProperties, objectMapper, List.of(chatClientFactory)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported LLM provider: 'unsupported'");
    }

    private LlmModelServiceImpl buildServiceWithDefaultAndSpeedModels() {
        LlmProperties llmProperties = new LlmProperties(modelsJson(
                "\"default\":{\"provider\":\"openai\",\"apiKey\":\"key\",\"baseUrl\":null,\"model\":\"gpt\",\"name\":\"default\",\"options\":{}}",
                "\"speed\":{\"provider\":\"openai\",\"apiKey\":\"key\",\"baseUrl\":null,\"model\":\"gpt\",\"name\":\"speed\",\"options\":{}}"));
        when(chatClientFactory.supports("openai")).thenReturn(true);
        when(chatClientFactory.build(org.mockito.ArgumentMatchers.any(LlmModelConfig.class)))
                .thenAnswer(invocation -> {
                    LlmModelConfig config = invocation.getArgument(0);
                    return "speed".equals(config.name()) ? speedChatClient : defaultChatClient;
                });
        return new LlmModelServiceImpl(llmProperties, objectMapper, List.of(chatClientFactory));
    }

    @Test
    void getDefaultModel_shouldReturnDefaultChatClient() {
        // Given
        LlmModelServiceImpl service = buildServiceWithDefaultAndSpeedModels();

        // When
        ChatClient result = service.getDefaultModel();

        // Then
        assertThat(result).isEqualTo(defaultChatClient);
    }

    @Test
    void hasModel_shouldReturnTrue_whenModelConfigured() {
        // Given
        LlmModelServiceImpl service = buildServiceWithDefaultAndSpeedModels();

        // When & Then
        assertThat(service.hasModel("speed")).isTrue();
        assertThat(service.hasModel("unknown")).isFalse();
    }

    @Test
    void getModel_byKey_shouldReturnConfiguredClient_whenPresent() {
        // Given
        LlmModelServiceImpl service = buildServiceWithDefaultAndSpeedModels();

        // When
        ChatClient result = service.getModel(LlmModelKey.SPEED);

        // Then
        assertThat(result).isEqualTo(speedChatClient);
    }

    @Test
    void getModel_byKey_shouldFallBackToDefault_whenKeyNotConfigured() {
        // Given
        LlmModelServiceImpl service = buildServiceWithDefaultAndSpeedModels();

        // When
        ChatClient result = service.getModel(LlmModelKey.CATEGORIZER);

        // Then
        assertThat(result).isEqualTo(defaultChatClient);
    }

    @Test
    void getModel_byName_shouldFallBackToDefault_whenNameIsBlank() {
        // Given
        LlmModelServiceImpl service = buildServiceWithDefaultAndSpeedModels();

        // When
        ChatClient result = service.getModel("   ");

        // Then
        assertThat(result).isEqualTo(defaultChatClient);
    }

    @Test
    void getModel_byName_shouldFallBackToDefault_whenNameIsNull() {
        // Given
        LlmModelServiceImpl service = buildServiceWithDefaultAndSpeedModels();

        // When
        ChatClient result = service.getModel((String) null);

        // Then
        assertThat(result).isEqualTo(defaultChatClient);
    }

    @Test
    void getModel_byName_shouldReturnConfiguredClient_whenNameConfigured() {
        // Given
        LlmModelServiceImpl service = buildServiceWithDefaultAndSpeedModels();

        // When
        ChatClient result = service.getModel("speed");

        // Then
        assertThat(result).isEqualTo(speedChatClient);
    }

    @Test
    void getModel_byName_shouldThrowParamException_whenNameNotConfigured() {
        // Given
        LlmModelServiceImpl service = buildServiceWithDefaultAndSpeedModels();

        // When & Then
        assertThatThrownBy(() -> service.getModel("unknown"))
                .isInstanceOf(ParamException.class);
    }

    @Test
    void hasSpeedModel_shouldReturnTrue_whenSpeedModelConfigured() {
        // Given
        LlmModelServiceImpl service = buildServiceWithDefaultAndSpeedModels();

        // When & Then
        assertThat(service.hasSpeedModel()).isTrue();
    }

    @Test
    void hasCategorizerModel_shouldReturnFalse_whenCategorizerModelNotConfigured() {
        // Given
        LlmModelServiceImpl service = buildServiceWithDefaultAndSpeedModels();

        // When & Then
        assertThat(service.hasCategorizerModel()).isFalse();
    }

    @Test
    void hasLongContextModel_shouldReturnFalse_whenLongContextModelNotConfigured() {
        // Given
        LlmModelServiceImpl service = buildServiceWithDefaultAndSpeedModels();

        // When & Then
        assertThat(service.hasLongContextModel()).isFalse();
    }

    @Test
    void constructor_shouldSelectFirstSupportingFactory_whenMultipleFactoriesProvided() {
        // Given
        ChatClientFactory nonSupportingFactory = org.mockito.Mockito.mock(ChatClientFactory.class);
        when(nonSupportingFactory.supports("openai")).thenReturn(false);
        when(chatClientFactory.supports("openai")).thenReturn(true);
        when(chatClientFactory.build(org.mockito.ArgumentMatchers.any(LlmModelConfig.class))).thenReturn(defaultChatClient);
        LlmProperties llmProperties = new LlmProperties(modelsJson(
                "\"default\":{\"provider\":\"openai\",\"apiKey\":\"key\",\"baseUrl\":null,\"model\":\"gpt\",\"name\":\"default\",\"options\":{}}"));

        // When
        LlmModelServiceImpl service = new LlmModelServiceImpl(llmProperties, objectMapper, List.of(nonSupportingFactory, chatClientFactory));

        // Then
        assertThat(service.getDefaultModel()).isEqualTo(defaultChatClient);
    }
}
