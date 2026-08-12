package com.opendatajungle.conversation.api.infra.service.chatpreprocessor;

import com.opendatajungle.conversation.api.infra.model.ChatContext;
import com.opendatajungle.conversation.api.infra.properties.ChatProperties;
import com.opendatajungle.conversation.api.shared.util.StringUtils;

public class DefaultSystemPromptPreProcessor implements ChatPreProcessor {
    public static final int ORDER = 10;
    public static final String ADDITIONAL_CONTEXT_USER_DEFINED_LOWER_PRIORITY = "\n\n--- ADDITIONAL CONTEXT (user-defined, lower priority) ---\n";

    private final ChatProperties chatProperties;

    public DefaultSystemPromptPreProcessor(ChatProperties chatProperties) {
        this.chatProperties = chatProperties;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    public ChatContext process(ChatContext chatContext) {
        return chatContext.withSystemMessage(composeSystemMessage(chatProperties.preProcessors().defaultSystemPrompt().prompt(), chatContext.systemMessage()));
    }

    private String composeSystemMessage(String basePrompt, String userSystemMessage) {
        if (StringUtils.isNullOrBlank(userSystemMessage)) {
            return basePrompt;
        }
        return basePrompt + ADDITIONAL_CONTEXT_USER_DEFINED_LOWER_PRIORITY + userSystemMessage;
    }
}

