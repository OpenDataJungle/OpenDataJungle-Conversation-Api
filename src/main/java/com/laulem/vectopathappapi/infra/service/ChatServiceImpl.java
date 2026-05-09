package com.laulem.vectopathappapi.infra.service;

import com.laulem.vectopathappapi.business.model.ChatResult;
import com.laulem.vectopathappapi.business.model.SendChatMessageCommand;
import com.laulem.vectopathappapi.business.service.ChatService;
import com.laulem.vectopathappapi.infra.model.ChatContext;
import com.laulem.vectopathappapi.infra.service.chatpreprocessor.ChatPreProcessingOrchestrator;
import com.laulem.vectopathappapi.infra.tool.ChatRequestHolder;
import com.laulem.vectopathappapi.infra.tool.SemanticSearchTool;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnMissingBean(ChatService.class)
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
                    .advisors(MessageChatMemoryAdvisor.builder(chatMemory)
                            .conversationId(sendChatMessageCommand.conversationId().toString())
                            .build())
                    .system(chatContext.systemMessage())
                    .user(chatContext.userMessage());

            if (chatContext.includeSearchTool()) {
                spec = spec.tools(semanticSearchTool);
            }

            String reply = spec
                    .toolCallbacks(mcpClientService.getRequiredToolCallbacksWithAdditional(sendChatMessageCommand.enabledTools()))
                    .call()
                    .content();

            return new ChatResult(reply, chatRequestHolder.getToolResults());

        } finally {
            chatRequestHolder.clear();
        }
    }
}
