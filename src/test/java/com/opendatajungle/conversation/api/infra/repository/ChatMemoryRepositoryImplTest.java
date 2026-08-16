package com.opendatajungle.conversation.api.infra.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opendatajungle.conversation.api.business.model.ToolResult;
import com.opendatajungle.conversation.api.infra.entity.ConversationMessageEntity;
import com.opendatajungle.conversation.api.infra.properties.ChatProperties;
import com.opendatajungle.conversation.api.infra.tool.ChatRequestHolder;
import com.opendatajungle.conversation.api.infra.tool.TransientContentMarker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatMemoryRepositoryImplTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UUID conversationId = UUID.randomUUID();
    @Mock
    private ConversationMessageJpaRepository conversationMessageRepository;
    @Mock
    private ChatRequestHolder chatRequestHolder;
    private ChatMemoryRepositoryImpl chatMemoryRepositoryImpl;

    private ChatProperties buildChatProperties(int maxContextTokens) {
        ChatProperties.PreProcessorsProperties.BasicResourceManagerProperties basicResourceManagerProperties =
                new ChatProperties.PreProcessorsProperties.BasicResourceManagerProperties(0, "required", "into-prompt", "too-long");
        ChatProperties.PreProcessorsProperties preProcessorsProperties = new ChatProperties.PreProcessorsProperties(
                new ChatProperties.PreProcessorsProperties.DefaultSystemPromptProperties("prompt"),
                new ChatProperties.PreProcessorsProperties.ResourceCategorizationProperties("categorizer prompt"),
                basicResourceManagerProperties);
        return new ChatProperties(maxContextTokens, preProcessorsProperties);
    }

    @BeforeEach
    void setUp() {
        chatMemoryRepositoryImpl = new ChatMemoryRepositoryImpl(buildChatProperties(100), conversationMessageRepository, chatRequestHolder, objectMapper);
    }

    private ConversationMessageEntity buildEntity(UUID id, String type, String content) {
        ConversationMessageEntity entity = new ConversationMessageEntity();
        entity.setId(id);
        entity.setConversationId(conversationId);
        entity.setType(type);
        entity.setContent(content);
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }

    @Test
    void findConversationIds_shouldReturnStringIdsFromRepository() {
        // Given
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        when(conversationMessageRepository.findDistinctConversationIds()).thenReturn(List.of(id1, id2));

        // When
        List<String> result = chatMemoryRepositoryImpl.findConversationIds();

        // Then
        assertThat(result).containsExactlyInAnyOrder(id1.toString(), id2.toString());
    }

    @Test
    void findByConversationId_shouldMapEntitiesToMessagesAndMarkAlreadySaved() {
        // Given
        when(conversationMessageRepository.findAllByConversationIdAndInContextTrueOrderByCreatedAtAsc(conversationId))
                .thenReturn(List.of(
                        buildEntity(UUID.randomUUID(), "USER", "user content"),
                        buildEntity(UUID.randomUUID(), "ASSISTANT", "assistant content"),
                        buildEntity(UUID.randomUUID(), "SYSTEM", "system content")));

        // When
        List<Message> result = chatMemoryRepositoryImpl.findByConversationId(conversationId.toString());

        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get(0)).isInstanceOf(UserMessage.class);
        assertThat(result.get(0).getText()).isEqualTo("user content");
        assertThat(result.get(1)).isInstanceOf(AssistantMessage.class);
        assertThat(result.get(1).getText()).isEqualTo("assistant content");
        assertThat(result.get(2)).isInstanceOf(SystemMessage.class);
        assertThat(result.get(2).getText()).isEqualTo("system content");
        result.forEach(message -> assertThat(message.getMetadata()).containsEntry("alreadySaved", true));
    }

    @Test
    void findByConversationId_shouldThrowIllegalStateException_whenMessageTypeUnknown() {
        // Given
        when(conversationMessageRepository.findAllByConversationIdAndInContextTrueOrderByCreatedAtAsc(conversationId))
                .thenReturn(List.of(buildEntity(UUID.randomUUID(), "UNKNOWN", "content")));

        // When & Then
        assertThatThrownBy(() -> chatMemoryRepositoryImpl.findByConversationId(conversationId.toString()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unknown message type: UNKNOWN");
    }

    @Test
    void saveAll_shouldPersistOnlyNewMessages_whenSomeAreAlreadySaved() {
        // Given
        UserMessage alreadySaved = new UserMessage("already saved");
        alreadySaved.getMetadata().put(ChatMemoryRepositoryImpl.ALREADY_SAVED, true);
        UserMessage newMessage = new UserMessage("brand new");
        when(conversationMessageRepository.findAllByConversationIdAndInContextTrueOrderByCreatedAtDesc(conversationId))
                .thenReturn(List.of());

        // When
        chatMemoryRepositoryImpl.saveAll(conversationId.toString(), List.of(alreadySaved, newMessage));

        // Then
        ArgumentCaptor<List<ConversationMessageEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(conversationMessageRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().getFirst().getContent()).isEqualTo("brand new");
        assertThat(captor.getValue().getFirst().getType()).isEqualTo("USER");
    }

    @Test
    void saveAll_shouldNotCallRepositorySaveAll_whenNoNewMessages() {
        // Given
        UserMessage alreadySaved = new UserMessage("already saved");
        alreadySaved.getMetadata().put(ChatMemoryRepositoryImpl.ALREADY_SAVED, true);
        when(conversationMessageRepository.findAllByConversationIdAndInContextTrueOrderByCreatedAtDesc(conversationId))
                .thenReturn(List.of());

        // When
        chatMemoryRepositoryImpl.saveAll(conversationId.toString(), List.of(alreadySaved));

        // Then
        verify(conversationMessageRepository, never()).saveAll(anyList());
    }

    @Test
    void saveAll_shouldStripTransientContent_whenSavingNewUserMessage() {
        // Given
        String wrapped = TransientContentMarker.wrap("resource content") + "actual question";
        UserMessage newMessage = new UserMessage(wrapped);
        when(conversationMessageRepository.findAllByConversationIdAndInContextTrueOrderByCreatedAtDesc(conversationId))
                .thenReturn(List.of());

        // When
        chatMemoryRepositoryImpl.saveAll(conversationId.toString(), List.of(newMessage));

        // Then
        ArgumentCaptor<List<ConversationMessageEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(conversationMessageRepository).saveAll(captor.capture());
        assertThat(captor.getValue().getFirst().getContent()).isEqualTo("actual question");
    }

    @Test
    void saveAll_shouldPersistToolResults_whenAssistantMessageHasResults() {
        // Given
        AssistantMessage assistantMessage = new AssistantMessage("assistant reply");
        List<ToolResult> toolResults = List.of(new ToolResult("tool-id", "query", Map.of("key", "value")));
        when(chatRequestHolder.getToolResults()).thenReturn(toolResults);
        when(conversationMessageRepository.findAllByConversationIdAndInContextTrueOrderByCreatedAtDesc(conversationId))
                .thenReturn(List.of());

        // When
        chatMemoryRepositoryImpl.saveAll(conversationId.toString(), List.of(assistantMessage));

        // Then
        ArgumentCaptor<List<ConversationMessageEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(conversationMessageRepository).saveAll(captor.capture());
        ConversationMessageEntity savedEntity = captor.getValue().getFirst();
        assertThat(savedEntity.getType()).isEqualTo("ASSISTANT");
        assertThat(savedEntity.getToolResults()).contains("tool-id").contains("query");
    }

    @Test
    void saveAll_shouldNotSetToolResults_whenAssistantMessageHasNoToolResults() {
        // Given
        AssistantMessage assistantMessage = new AssistantMessage("assistant reply");
        when(chatRequestHolder.getToolResults()).thenReturn(List.of());
        when(conversationMessageRepository.findAllByConversationIdAndInContextTrueOrderByCreatedAtDesc(conversationId))
                .thenReturn(List.of());

        // When
        chatMemoryRepositoryImpl.saveAll(conversationId.toString(), List.of(assistantMessage));

        // Then
        ArgumentCaptor<List<ConversationMessageEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(conversationMessageRepository).saveAll(captor.capture());
        assertThat(captor.getValue().getFirst().getToolResults()).isNull();
    }

    @Test
    void saveAll_shouldLogAndContinue_whenToolResultsSerializationFails() {
        // Given
        AssistantMessage assistantMessage = new AssistantMessage("assistant reply");
        List<ToolResult> toolResults = List.of(new ToolResult("tool-id", "query", Map.of("bad", new Object())));
        when(chatRequestHolder.getToolResults()).thenReturn(toolResults);
        when(conversationMessageRepository.findAllByConversationIdAndInContextTrueOrderByCreatedAtDesc(conversationId))
                .thenReturn(List.of());

        // When
        chatMemoryRepositoryImpl.saveAll(conversationId.toString(), List.of(assistantMessage));

        // Then
        ArgumentCaptor<List<ConversationMessageEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(conversationMessageRepository).saveAll(captor.capture());
        assertThat(captor.getValue().getFirst().getToolResults()).isNull();
    }

    @Test
    void saveAll_shouldApplyTokenWindow_keepingOnlyMessagesWithinBudget() {
        // Given: a chat properties with a very small token budget
        chatMemoryRepositoryImpl = new ChatMemoryRepositoryImpl(buildChatProperties(2), conversationMessageRepository, chatRequestHolder, objectMapper);
        UUID keptId = UUID.randomUUID();
        UUID droppedId = UUID.randomUUID();
        // Each message content is 8 chars -> ~2 tokens; budget of 2 allows only the first message
        when(conversationMessageRepository.findAllByConversationIdAndInContextTrueOrderByCreatedAtDesc(conversationId))
                .thenReturn(List.of(
                        buildEntity(keptId, "USER", "12345678"),
                        buildEntity(droppedId, "USER", "12345678")));

        // When
        chatMemoryRepositoryImpl.saveAll(conversationId.toString(), List.of());

        // Then
        verify(conversationMessageRepository).setAllOutOfContextButIds(eq(conversationId), eq(List.of(keptId)));
    }

    @Test
    void saveAll_shouldNotSetOutOfContext_whenNoMessagesFitInWindow() {
        // Given
        when(conversationMessageRepository.findAllByConversationIdAndInContextTrueOrderByCreatedAtDesc(conversationId))
                .thenReturn(List.of());

        // When
        chatMemoryRepositoryImpl.saveAll(conversationId.toString(), List.of());

        // Then
        verify(conversationMessageRepository, never()).setAllOutOfContextButIds(eq(conversationId), anyList());
    }

    @Test
    void deleteByConversationId_shouldDelegateToRepository() {
        // Given & When
        chatMemoryRepositoryImpl.deleteByConversationId(conversationId.toString());

        // Then
        verify(conversationMessageRepository, times(1)).setAllOutOfContext(conversationId);
    }
}
