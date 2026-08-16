package com.opendatajungle.conversation.api.infra.properties;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChatPropertiesTest {

    private final ChatProperties.PreProcessorsProperties.BasicResourceManagerProperties basicResourceManagerProperties =
            new ChatProperties.PreProcessorsProperties.BasicResourceManagerProperties(1000, "required", "into-prompt", "too-long");

    private final ChatProperties.PreProcessorsProperties preProcessorsProperties = new ChatProperties.PreProcessorsProperties(
            new ChatProperties.PreProcessorsProperties.DefaultSystemPromptProperties("prompt"),
            new ChatProperties.PreProcessorsProperties.ResourceCategorizationProperties("categorizer prompt"),
            basicResourceManagerProperties);

    @Test
    void constructor_shouldKeepProvidedMaxContextTokens_whenPositiveAndAboveThreshold() {
        // Given & When
        ChatProperties chatProperties = new ChatProperties(5000, preProcessorsProperties);

        // Then
        assertThat(chatProperties.maxContextTokens()).isEqualTo(5000);
    }

    @Test
    void constructor_shouldDefaultMaxContextTokens_whenNotPositive() {
        // Given & When
        ChatProperties chatProperties = new ChatProperties(0, preProcessorsProperties);

        // Then
        assertThat(chatProperties.maxContextTokens()).isEqualTo(50000);
    }

    @Test
    void constructor_shouldDefaultMaxContextTokens_whenNegative() {
        // Given & When
        ChatProperties chatProperties = new ChatProperties(-10, preProcessorsProperties);

        // Then
        assertThat(chatProperties.maxContextTokens()).isEqualTo(50000);
    }

    @Test
    void constructor_shouldThrowIllegalArgumentException_whenMaxContextTokensBelowMaxFileContentsTokens() {
        // Given
        ChatProperties.PreProcessorsProperties.BasicResourceManagerProperties tooLargeFileTokens =
                new ChatProperties.PreProcessorsProperties.BasicResourceManagerProperties(6000, "required", "into-prompt", "too-long");
        ChatProperties.PreProcessorsProperties preProcessors = new ChatProperties.PreProcessorsProperties(
                preProcessorsProperties.defaultSystemPrompt(),
                preProcessorsProperties.resourceCategorization(),
                tooLargeFileTokens);

        // When & Then
        assertThatThrownBy(() -> new ChatProperties(5000, preProcessors))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("maxContextTokens must be greater than maxFileContentsTokens");
    }

    @Test
    void constructor_shouldSucceed_whenMaxContextTokensEqualsMaxFileContentsTokens() {
        // Given & When
        ChatProperties chatProperties = new ChatProperties(1000, preProcessorsProperties);

        // Then
        assertThat(chatProperties.maxContextTokens()).isEqualTo(1000);
    }
}
