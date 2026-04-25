package com.laulem.vectopathappapi.infra.service;

import com.laulem.vectopathappapi.business.model.SendChatMessageCommand;
import com.laulem.vectopathappapi.business.model.ChatResult;
import com.laulem.vectopathappapi.business.service.ChatService;
import com.laulem.vectopathappapi.infra.properties.ChatProperties;
import com.laulem.vectopathappapi.infra.tool.ChatRequestHolder;
import com.laulem.vectopathappapi.infra.tool.SemanticSearchTool;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
public class ChatServiceImpl implements ChatService {
    private final LlmModelService llmModelService;
    private final SemanticSearchTool semanticSearchTool;
    private final McpClientService mcpClientService;
    private final ChatMemory chatMemory;
    private final ChatRequestHolder chatRequestHolder;
    private final ChatProperties chatProperties;

    public ChatServiceImpl(LlmModelService llmModelService,
                           SemanticSearchTool semanticSearchTool,
                           McpClientService mcpClientService,
                           ChatMemory chatMemory,
                           ChatRequestHolder chatRequestHolder,
                           ChatProperties chatProperties) {
        this.llmModelService = llmModelService;
        this.semanticSearchTool = semanticSearchTool;
        this.mcpClientService = mcpClientService;
        this.chatMemory = chatMemory;
        this.chatRequestHolder = chatRequestHolder;
        this.chatProperties = chatProperties;
    }

    @Override
    @Transactional
    public ChatResult chat(SendChatMessageCommand sendChatMessageCommand, String systemMessage) {
        try {
            chatRequestHolder.setResourceIds(sendChatMessageCommand.resourceIds());
            boolean hasResourceIds = !CollectionUtils.isEmpty(sendChatMessageCommand.resourceIds());
            String reply = llmModelService.getModel(sendChatMessageCommand.llmModel())
                    .prompt()
                    .advisors(MessageChatMemoryAdvisor.builder(chatMemory)
                            .conversationId(sendChatMessageCommand.conversationId().toString())
                            .build())
                    .system(getEffectiveSystemMessage(systemMessage, hasResourceIds))
                    .user(sendChatMessageCommand.message())
                    .tools(semanticSearchTool)
                    .toolCallbacks(mcpClientService.getRequiredToolCallbacksWithAdditional(sendChatMessageCommand.enabledTools()))
                    .call()
                    .content();
            return new ChatResult(reply, chatRequestHolder.getToolResults());
        } finally {
            chatRequestHolder.clear();
        }
    }

    private String getEffectiveSystemMessage(final String systemMessage, final boolean hasResourceIds) {
        String basePrompt = (hasResourceIds && chatProperties.resourceIdsRequiredPrompt() != null)
                ? chatProperties.resourceIdsRequiredPrompt()
                : chatProperties.defaultSystemPrompt();
        if (systemMessage == null || systemMessage.isBlank()) {
            return basePrompt;
        }
        // Explicit delimiter to contain user-supplied instructions in a distinct section,
        // reducing prompt injection risk (preventing override of the base system prompt).
        return basePrompt + "\n\n--- ADDITIONAL CONTEXT (user-defined, lower priority) ---\n" + systemMessage;
    }
}
