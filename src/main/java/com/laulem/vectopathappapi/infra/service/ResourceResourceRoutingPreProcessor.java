package com.laulem.vectopathappapi.infra.service;

import com.laulem.vectopathappapi.infra.model.ChatContext;
import com.laulem.vectopathappapi.infra.model.ResourceContent;
import com.laulem.vectopathappapi.infra.model.ResourceRoutingStrategy;
import com.laulem.vectopathappapi.infra.properties.ChatProperties;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.UUID;

@Slf4j
public class ResourceResourceRoutingPreProcessor implements ChatPreProcessor {
    public static final int ORDER = 100;
    private static final int CHARS_PER_TOKEN = 4;

    private final LlmModelService llmModelService;
    private final ResourceCategorizerService resourceCategorizerService;
    private final ResourceContentService resourceContentService;
    private final ChatProperties chatProperties;

    public ResourceResourceRoutingPreProcessor(LlmModelService llmModelService,
                                               ResourceCategorizerService resourceCategorizerService,
                                               ResourceContentService resourceContentService,
                                               ChatProperties chatProperties) {
        this.llmModelService = llmModelService;
        this.resourceCategorizerService = resourceCategorizerService;
        this.resourceContentService = resourceContentService;
        this.chatProperties = chatProperties;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    public ChatContext process(ChatContext chatContext) {
        List<UUID> resourceIds = chatContext.command().resourceIds();
        boolean hasResourceIds = !CollectionUtils.isEmpty(resourceIds);

        if (!hasResourceIds) {
            return chatContext.withSystemMessage(composeSystemMessage(chatProperties.defaultSystemPrompt(), chatContext.systemMessage()));
        }

        if (llmModelService.hasCategorizerModel()) {
            ResourceRoutingStrategy strategy = resourceCategorizerService
                    .categorize(chatContext.userMessage(), resourceIds)
                    .strategy();

            log.debug("Categorizer routing decision for conversation {}: {}", chatContext.command().conversationId(), strategy);

            if (strategy == ResourceRoutingStrategy.INCLUDE_IN_PROMPT) {
                return  tryIncludeInPrompt(chatContext, resourceIds);
            }
        }

        return getDefaultChatContext(chatContext);
    }

    private @NonNull ChatContext getDefaultChatContext(final ChatContext chatContext) {
        return chatContext.withSystemMessage(composeSystemMessage(chatProperties.resourceIdsRequiredPrompt(), chatContext.systemMessage()));
    }

    private ChatContext tryIncludeInPrompt(ChatContext chatContext, List<UUID> resourceIds) {
        List<ResourceContent> contents = resourceContentService.fetchContents(resourceIds);
        if (contents.isEmpty()) {
            log.debug("No content retrieved for resource IDs {}", resourceIds);
            return chatContext;
        }

        String resourceContent = buildResourceContent(contents);
        int estimatedTokens = (resourceContent.length() + chatContext.userMessage().length()) / CHARS_PER_TOKEN;

        if (estimatedTokens > chatProperties.maxContextTokens()) {
            return chatContext.withSystemMessage(composeSystemMessage(chatProperties.defaultSystemPrompt(), chatContext.systemMessage())
                    + "\n\n--- RESSOURCES ---\n" + "Content too long to include in prompt (estimated tokens: " + estimatedTokens + "). Please refine your resource selection.");
        }

        String enrichedSystemMessage = composeSystemMessage(chatProperties.defaultSystemPrompt(), chatContext.systemMessage())
                + "\n\n--- RESSOURCES ---\n" + resourceContent;

        return chatContext
                .withSystemMessage(enrichedSystemMessage)
                .withIncludeSearchTool(false);
    }

    private String composeSystemMessage(String basePrompt, String userSystemMessage) {
        if (userSystemMessage == null || userSystemMessage.isBlank()) {
            return basePrompt;
        }
        return basePrompt + "\n\n--- ADDITIONAL CONTEXT (user-defined, lower priority) ---\n" + userSystemMessage;
    }

    private String buildResourceContent(List<ResourceContent> contents) {
        StringBuilder sb = new StringBuilder();
        for (ResourceContent resource : contents) {
            sb.append("--- Ressource : ").append(resource.name()).append(" ---\n");
            sb.append(resource.content()).append('\n');
            sb.append('\n');
        }
        return sb.toString();
    }
}
