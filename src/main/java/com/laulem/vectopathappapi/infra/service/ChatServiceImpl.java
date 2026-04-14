package com.laulem.vectopathappapi.infra.service;

import com.laulem.vectopathappapi.business.model.ChatResult;
import com.laulem.vectopathappapi.business.service.ChatService;
import com.laulem.vectopathappapi.infra.properties.ChatProperties;
import com.laulem.vectopathappapi.infra.tool.ChatRequestHolder;
import com.laulem.vectopathappapi.infra.tool.SemanticSearchTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.UUID;

@Service
public class ChatServiceImpl implements ChatService {
    private final ChatClient chatClient;
    private final SemanticSearchTool semanticSearchTool;
    private final ChatMemory chatMemory;
    private final ChatRequestHolder chatRequestHolder;
    private final ChatProperties chatProperties;

    public ChatServiceImpl(ChatClient.Builder chatClientBuilder,
                           SemanticSearchTool semanticSearchTool,
                           ChatMemory chatMemory,
                           ChatRequestHolder chatRequestHolder,
                           ChatProperties chatProperties) {
        this.chatClient = chatClientBuilder.build();
        this.semanticSearchTool = semanticSearchTool;
        this.chatMemory = chatMemory;
        this.chatRequestHolder = chatRequestHolder;
        this.chatProperties = chatProperties;
    }

    @Override
    @Transactional
    public ChatResult chat(UUID conversationId, String systemMessage, String message, List<UUID> resourceIds) {
        try {
            chatRequestHolder.setResourceIds(resourceIds);
            boolean hasResourceIds = !CollectionUtils.isEmpty(resourceIds);
            String reply = chatClient.prompt()
                    .advisors(MessageChatMemoryAdvisor.builder(chatMemory)
                            .conversationId(conversationId.toString())
                            .build())
                    .system(getEffectiveSystemMessage(systemMessage, hasResourceIds))
                    .user(message)
                    .tools(semanticSearchTool)
                    .call()
                    .content();
            return new ChatResult(reply, chatRequestHolder.getToolResults());
        } finally {
            chatRequestHolder.clear();
        }
    }

    private String getEffectiveSystemMessage(final String systemMessage, final boolean hasResourceIds) {
        String basePrompt = (hasResourceIds && chatProperties.getResourceIdsRequiredPrompt() != null)
                ? chatProperties.getResourceIdsRequiredPrompt()
                : chatProperties.getDefaultSystemPrompt();
        return (systemMessage != null && !systemMessage.isBlank())
                ? basePrompt + "\n\n" + systemMessage
                : basePrompt;
    }
}
