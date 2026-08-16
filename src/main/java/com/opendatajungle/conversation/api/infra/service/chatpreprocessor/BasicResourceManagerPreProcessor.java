package com.opendatajungle.conversation.api.infra.service.chatpreprocessor;

import com.opendatajungle.commons.util.StringUtils;
import com.opendatajungle.conversation.api.infra.model.ChatContext;
import com.opendatajungle.conversation.api.infra.model.ResourceContent;
import com.opendatajungle.conversation.api.infra.model.ResourceRoutingStrategy;
import com.opendatajungle.conversation.api.infra.properties.ChatProperties;
import com.opendatajungle.conversation.api.infra.service.ResourceContentService;
import com.opendatajungle.conversation.api.infra.tool.TransientContentMarker;
import com.opendatajungle.conversation.api.shared.util.TokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.UUID;

/**
 * Pre-processor that handles resource management for chat contexts.
 * It processes the chat context based on the specified resource routing strategy,
 * either including resources in the prompt or performing an internal search.
 */
@Slf4j
public class BasicResourceManagerPreProcessor implements ChatPreProcessor {
    public static final String ADDITIONAL_CONTEXT_USER_DEFINED_LOWER_PRIORITY = "\n\n--- ADDITIONAL CONTEXT (user-defined, lower priority) ---\n";
    public static final int ORDER = 110;
    public static final String START_LIMITER = "--- ";
    public static final String JUMP_LINE = "\n";
    public static final String END_LIMITER = START_LIMITER + JUMP_LINE;
    public static final String BEGIN_FILE = START_LIMITER + " BEGIN FILE: ";
    public static final String END_FILE = START_LIMITER + " END FILE: ";

    private final ResourceContentService resourceContentService;
    private final ChatProperties chatProperties;

    public BasicResourceManagerPreProcessor(ResourceContentService resourceContentService,
                                            ChatProperties chatProperties) {
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
        if (CollectionUtils.isEmpty(resourceIds)) {
            return chatContext;
        }

        if (ResourceRoutingStrategy.INCLUDE_IN_PROMPT.equals(chatContext.resourceRoutingStrategy())) {
            return getResourcesInPromptChatContext(chatContext, resourceIds);
        } else if (ResourceRoutingStrategy.INTERNAL_SEARCH.equals(chatContext.resourceRoutingStrategy())) {
            String newSystemMessage = composeSystemMessage(chatProperties.preProcessors().basicResourceManager().resourceIdsRequiredPrompt(), chatContext.systemMessage());
            return chatContext.withSystemMessage(newSystemMessage);
        }

        return chatContext;
    }

    private ChatContext getResourcesInPromptChatContext(ChatContext chatContext, List<UUID> resourceIds) {
        List<ResourceContent> contents = resourceContentService.fetchContents(resourceIds);
        if (contents.isEmpty()) {
            return chatContext;
        }

        String resourceContent = buildResourceContentPrompt(contents);
        long estimatedTokens = TokenUtils.calculateToken(resourceContent);
        if (estimatedTokens > chatProperties.preProcessors().basicResourceManager().maxFileContentsTokens()) {
            return getTooLongResourcesChatContext(chatContext);
        }

        return getResourcesChatContext(chatContext, resourceContent);
    }

    private @NonNull ChatContext getResourcesChatContext(final ChatContext chatContext, final String resourceContent) {
        return chatContext
                .withUserMessage(TransientContentMarker.wrap(resourceContent) + chatContext.userMessage())
                .withIncludeSearchTool(false);
    }

    private @NonNull ChatContext getTooLongResourcesChatContext(final ChatContext chatContext) {
        String tooLongPrompt = chatProperties.preProcessors().basicResourceManager().tooLongPrompt();
        return chatContext
                .withSystemMessage(tooLongPrompt)
                .withUserMessage(TransientContentMarker.wrap(tooLongPrompt) + chatContext.userMessage());
    }

    private String buildResourceContentPrompt(List<ResourceContent> contents) {
        StringBuilder sb = new StringBuilder();
        sb.append(chatProperties.preProcessors().basicResourceManager().resourcesIntoPrompt()).append(JUMP_LINE);

        for (ResourceContent resource : contents) {
            sb.append(BEGIN_FILE).append(resource.name()).append(END_LIMITER);
            sb.append(resource.content()).append(JUMP_LINE);
            sb.append(END_FILE).append(resource.name()).append(END_LIMITER);
        }
        return sb.toString();
    }

    private String composeSystemMessage(String basePrompt, String userSystemMessage) {
        if (StringUtils.isNullOrBlank(userSystemMessage)) {
            return basePrompt;
        }
        return basePrompt + ADDITIONAL_CONTEXT_USER_DEFINED_LOWER_PRIORITY + userSystemMessage;
    }
}
