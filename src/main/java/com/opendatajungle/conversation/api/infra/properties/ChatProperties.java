package com.opendatajungle.conversation.api.infra.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Objects;

@ConfigurationProperties(prefix = "open-data-jungle.chat")
public record ChatProperties(
        int maxContextTokens,
        int maxInternalResourceSearchResults,
        PreProcessorsProperties preProcessors) {

    public ChatProperties {
        if (maxContextTokens <= 0) maxContextTokens = 50000;
        if (maxInternalResourceSearchResults <= 0) maxInternalResourceSearchResults = 3;
        int maxFileContentsTokens = Objects.requireNonNullElse(preProcessors.basicResourceManager().maxFileContentsTokens(), 0);
        if (maxContextTokens < maxFileContentsTokens)
            throw new IllegalArgumentException("maxContextTokens must be greater than maxFileContentsTokens");
    }

    public record PreProcessorsProperties(
            DefaultSystemPromptProperties defaultSystemPrompt,
            ResourceCategorizationProperties resourceCategorization,
            BasicResourceManagerProperties basicResourceManager) {

        public record DefaultSystemPromptProperties(String prompt) {
        }

        public record ResourceCategorizationProperties(String categorizerSystemPrompt) {
        }

        public record BasicResourceManagerProperties(
                int maxFileContentsTokens,
                String resourceIdsRequiredPrompt,
                String resourcesIntoPrompt,
                String tooLongPrompt) {
        }
    }
}
