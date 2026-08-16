package com.opendatajungle.conversation.api.infra.service;

import com.opendatajungle.conversation.api.business.model.ChatResult;
import com.opendatajungle.conversation.api.business.model.SendChatMessageCommand;
import com.opendatajungle.conversation.api.business.model.ToolResult;
import com.opendatajungle.conversation.api.infra.model.ChatContext;
import com.opendatajungle.conversation.api.infra.service.chatpreprocessor.ChatPreProcessingOrchestrator;
import com.opendatajungle.conversation.api.infra.tool.ChatRequestHolder;
import com.opendatajungle.conversation.api.infra.tool.SemanticSearchTool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock
    private LlmModelService llmModelService;

    @Mock
    private SemanticSearchTool semanticSearchTool;

    @Mock
    private McpClientService mcpClientService;

    @Mock
    private ChatMemory chatMemory;

    @Mock
    private ChatRequestHolder chatRequestHolder;

    @Mock
    private ChatPreProcessingOrchestrator chatPreProcessingOrchestrator;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    @InjectMocks
    private ChatServiceImpl chatServiceImpl;

    private final UUID conversationId = UUID.randomUUID();
    private final SendChatMessageCommand command = new SendChatMessageCommand(
            conversationId, "hello", List.of(UUID.randomUUID()), Set.of("tool-a"), "default");

    @Test
    void chat_shouldIncludeSearchToolAndReturnResult_whenContextIncludesSearchTool() {
        // Given
        ChatContext preProcessedContext = ChatContext.of(command, "system message").withIncludeSearchTool(true);
        List<ToolResult> toolResults = List.of(new ToolResult("tool-id", "query", java.util.Map.of()));
        ToolCallback[] toolCallbacks = new ToolCallback[0];

        when(chatPreProcessingOrchestrator.run(any(ChatContext.class))).thenReturn(preProcessedContext);
        when(llmModelService.getModel(command.llmModel())).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.advisors(any(Advisor.class))).thenReturn(requestSpec);
        when(requestSpec.system(preProcessedContext.systemMessage())).thenReturn(requestSpec);
        when(requestSpec.user(preProcessedContext.userMessage())).thenReturn(requestSpec);
        when(requestSpec.tools(semanticSearchTool)).thenReturn(requestSpec);
        when(mcpClientService.getRequiredToolCallbacksWithAdditional(command.enabledTools())).thenReturn(toolCallbacks);
        when(requestSpec.toolCallbacks(toolCallbacks)).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("assistant reply");
        when(chatRequestHolder.getToolResults()).thenReturn(toolResults);

        // When
        ChatResult result = chatServiceImpl.chat(command, "initial system message");

        // Then
        assertThat(result).isEqualTo(new ChatResult("assistant reply", toolResults));
        verify(chatRequestHolder).setResourceIds(command.resourceIds());
        verify(requestSpec).tools(semanticSearchTool);
        verify(chatRequestHolder).clear();
    }

    @Test
    void chat_shouldNotIncludeSearchTool_whenContextExcludesSearchTool() {
        // Given
        ChatContext preProcessedContext = ChatContext.of(command, "system message").withIncludeSearchTool(false);
        ToolCallback[] toolCallbacks = new ToolCallback[0];

        when(chatPreProcessingOrchestrator.run(any(ChatContext.class))).thenReturn(preProcessedContext);
        when(llmModelService.getModel(command.llmModel())).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.advisors(any(Advisor.class))).thenReturn(requestSpec);
        when(requestSpec.system(preProcessedContext.systemMessage())).thenReturn(requestSpec);
        when(requestSpec.user(preProcessedContext.userMessage())).thenReturn(requestSpec);
        when(mcpClientService.getRequiredToolCallbacksWithAdditional(command.enabledTools())).thenReturn(toolCallbacks);
        when(requestSpec.toolCallbacks(toolCallbacks)).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("assistant reply");
        when(chatRequestHolder.getToolResults()).thenReturn(List.of());

        // When
        ChatResult result = chatServiceImpl.chat(command, "initial system message");

        // Then
        assertThat(result.reply()).isEqualTo("assistant reply");
        verify(requestSpec, never()).tools(any());
        verify(chatRequestHolder).clear();
    }

    @Test
    void chat_shouldClearChatRequestHolder_evenWhenCallThrows() {
        // Given
        ChatContext preProcessedContext = ChatContext.of(command, "system message").withIncludeSearchTool(false);
        ToolCallback[] toolCallbacks = new ToolCallback[0];

        when(chatPreProcessingOrchestrator.run(any(ChatContext.class))).thenReturn(preProcessedContext);
        when(llmModelService.getModel(command.llmModel())).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.advisors(any(Advisor.class))).thenReturn(requestSpec);
        when(requestSpec.system(preProcessedContext.systemMessage())).thenReturn(requestSpec);
        when(requestSpec.user(preProcessedContext.userMessage())).thenReturn(requestSpec);
        when(mcpClientService.getRequiredToolCallbacksWithAdditional(command.enabledTools())).thenReturn(toolCallbacks);
        when(requestSpec.toolCallbacks(toolCallbacks)).thenReturn(requestSpec);
        when(requestSpec.call()).thenThrow(new RuntimeException("boom"));

        // When & Then
        assertThatThrownBy(() -> chatServiceImpl.chat(command, "initial system message"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("boom");
        verify(chatRequestHolder).clear();
    }
}
