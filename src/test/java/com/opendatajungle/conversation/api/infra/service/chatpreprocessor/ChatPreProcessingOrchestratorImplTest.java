package com.opendatajungle.conversation.api.infra.service.chatpreprocessor;

import com.opendatajungle.conversation.api.business.model.SendChatMessageCommand;
import com.opendatajungle.conversation.api.infra.model.ChatContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatPreProcessingOrchestratorImplTest {

    private final SendChatMessageCommand command = new SendChatMessageCommand(
            UUID.randomUUID(), "hello", null, null, "default");
    @Mock
    private ChatPreProcessor firstProcessor;
    @Mock
    private ChatPreProcessor secondProcessor;

    @Test
    void run_shouldApplyProcessorsInOrderOfGetOrder() {
        // Given
        when(firstProcessor.getOrder()).thenReturn(20);
        when(secondProcessor.getOrder()).thenReturn(10);

        ChatContext initial = ChatContext.of(command, "system");
        ChatContext afterSecond = initial.withUserMessage("after-second");
        ChatContext afterFirst = afterSecond.withUserMessage("after-first");

        when(secondProcessor.process(initial)).thenReturn(afterFirst);
        when(firstProcessor.process(afterFirst)).thenReturn(afterSecond);

        ChatPreProcessingOrchestratorImpl orchestrator = new ChatPreProcessingOrchestratorImpl(List.of(firstProcessor, secondProcessor));

        // When
        ChatContext result = orchestrator.run(initial);

        // Then
        assertThat(result).isEqualTo(afterSecond);
    }

    @Test
    void run_shouldReturnInitialContext_whenNoProcessors() {
        // Given
        ChatPreProcessingOrchestratorImpl orchestrator = new ChatPreProcessingOrchestratorImpl(List.of());
        ChatContext initial = ChatContext.of(command, "system");

        // When
        ChatContext result = orchestrator.run(initial);

        // Then
        assertThat(result).isEqualTo(initial);
    }
}
