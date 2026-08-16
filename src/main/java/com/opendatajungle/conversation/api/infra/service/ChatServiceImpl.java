package com.opendatajungle.conversation.api.infra.service;

import com.opendatajungle.conversation.api.business.model.ChatResult;
import com.opendatajungle.conversation.api.business.model.SendChatMessageCommand;
import com.opendatajungle.conversation.api.business.service.ChatService;
import com.opendatajungle.conversation.api.infra.model.ChatContext;
import com.opendatajungle.conversation.api.infra.service.chatpreprocessor.ChatPreProcessingOrchestrator;
import com.opendatajungle.conversation.api.infra.tool.ChatRequestHolder;
import com.opendatajungle.conversation.api.infra.tool.SemanticSearchTool;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.transaction.annotation.Transactional;

public class ChatServiceImpl implements ChatService {
    private final LlmModelService llmModelService;
    private final SemanticSearchTool semanticSearchTool;
    private final McpClientService mcpClientService;
    private final ChatMemory chatMemory;
    private final ChatRequestHolder chatRequestHolder;
    private final ChatPreProcessingOrchestrator chatPreProcessingOrchestrator;

    public ChatServiceImpl(LlmModelService llmModelService,
                           SemanticSearchTool semanticSearchTool,
                           McpClientService mcpClientService,
                           ChatMemory chatMemory,
                           ChatRequestHolder chatRequestHolder,
                           ChatPreProcessingOrchestrator chatPreProcessingOrchestrator) {
        this.llmModelService = llmModelService;
        this.semanticSearchTool = semanticSearchTool;
        this.mcpClientService = mcpClientService;
        this.chatMemory = chatMemory;
        this.chatRequestHolder = chatRequestHolder;
        this.chatPreProcessingOrchestrator = chatPreProcessingOrchestrator;
    }

    @Override
    @Transactional
    public ChatResult chat(SendChatMessageCommand sendChatMessageCommand, String systemMessage) {
        try {
            chatRequestHolder.setResourceIds(sendChatMessageCommand.resourceIds());
            ChatContext chatContext = chatPreProcessingOrchestrator.run(ChatContext.of(sendChatMessageCommand, systemMessage));

            var spec = llmModelService.getModel(sendChatMessageCommand.llmModel())
                    .prompt()
                    .advisors(a -> a.advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                            .param(ChatMemory.CONVERSATION_ID, sendChatMessageCommand.conversationId().toString()))
                    .system(chatContext.systemMessage())
                    .user(chatContext.userMessage());

            if (chatContext.includeSearchTool()) {
                spec = spec.tools(semanticSearchTool);
            }

            String reply = spec
                    .tools(mcpClientService.getRequiredToolCallbacksWithAdditional(sendChatMessageCommand.enabledTools()))
                    .call()
                    .content();

            return new ChatResult(reply, chatRequestHolder.getToolResults());

        } finally {
            chatRequestHolder.clear();
        }
    }
}
