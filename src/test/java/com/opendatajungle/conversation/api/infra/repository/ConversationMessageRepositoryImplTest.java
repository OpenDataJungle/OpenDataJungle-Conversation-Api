package com.opendatajungle.conversation.api.infra.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opendatajungle.conversation.api.business.model.ConversationMessage;
import com.opendatajungle.conversation.api.business.model.ToolResult;
import com.opendatajungle.conversation.api.infra.entity.ConversationMessageEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationMessageRepositoryImplTest {

    private final UUID conversationId = UUID.randomUUID();
    private final UUID messageId = UUID.randomUUID();
    private final LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
    @Mock
    private ConversationMessageJpaRepository conversationMessageRepository;
    private ObjectMapper objectMapper;
    private ConversationMessageRepositoryImpl conversationMessageRepositoryImpl;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        conversationMessageRepositoryImpl = new ConversationMessageRepositoryImpl(conversationMessageRepository, objectMapper);
    }

    private ConversationMessageEntity buildEntity(String toolResultsJson) {
        ConversationMessageEntity entity = new ConversationMessageEntity();
        entity.setId(messageId);
        entity.setConversationId(conversationId);
        entity.setType("ASSISTANT");
        entity.setContent("content");
        entity.setCreatedAt(createdAt);
        entity.setToolResults(toolResultsJson);
        return entity;
    }

    @Test
    void findAllByConversationId_shouldMapEntityWithoutToolResults_whenToolResultsIsNull() {
        // Given
        when(conversationMessageRepository.findAllByConversationIdOrderByCreatedAtAsc(conversationId))
                .thenReturn(List.of(buildEntity(null)));

        // When
        List<ConversationMessage> result = conversationMessageRepositoryImpl.findAllByConversationId(conversationId);

        // Then
        assertThat(result).hasSize(1);
        ConversationMessage message = result.getFirst();
        assertThat(message.id()).isEqualTo(messageId);
        assertThat(message.conversationId()).isEqualTo(conversationId);
        assertThat(message.type()).isEqualTo("ASSISTANT");
        assertThat(message.content()).isEqualTo("content");
        assertThat(message.createdAt()).isEqualTo(createdAt);
        assertThat(message.toolResults()).isNull();
    }

    @Test
    void findAllByConversationId_shouldDeserializeToolResults_whenPresent() throws Exception {
        // Given
        List<ToolResult> toolResults = List.of(new ToolResult("tool-id", "query", Map.of("key", "value")));
        when(conversationMessageRepository.findAllByConversationIdOrderByCreatedAtAsc(conversationId))
                .thenReturn(List.of(buildEntity(objectMapper.writeValueAsString(toolResults))));

        // When
        List<ConversationMessage> result = conversationMessageRepositoryImpl.findAllByConversationId(conversationId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().toolResults()).isEqualTo(toolResults);
    }

    @Test
    void findAllByConversationId_shouldReturnNullToolResults_whenJsonIsMalformed() {
        // Given
        when(conversationMessageRepository.findAllByConversationIdOrderByCreatedAtAsc(conversationId))
                .thenReturn(List.of(buildEntity("not-json")));

        // When
        List<ConversationMessage> result = conversationMessageRepositoryImpl.findAllByConversationId(conversationId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().toolResults()).isNull();
    }

    @Test
    void findAllByConversationId_shouldReturnEmptyList_whenNoMessages() {
        // Given
        when(conversationMessageRepository.findAllByConversationIdOrderByCreatedAtAsc(conversationId)).thenReturn(List.of());

        // When
        List<ConversationMessage> result = conversationMessageRepositoryImpl.findAllByConversationId(conversationId);

        // Then
        assertThat(result).isEmpty();
    }
}
