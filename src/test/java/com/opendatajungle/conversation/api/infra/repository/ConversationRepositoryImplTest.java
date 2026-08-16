package com.opendatajungle.conversation.api.infra.repository;

import com.opendatajungle.conversation.api.business.model.Conversation;
import com.opendatajungle.conversation.api.infra.entity.ConversationEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationRepositoryImplTest {

    private final UUID conversationId = UUID.randomUUID();
    private final LocalDateTime createdAt = LocalDateTime.of(2024, Month.JANUARY, 1, 10, 0);
    private final LocalDateTime lastMessageAt = LocalDateTime.of(2024, Month.JANUARY, 2, 10, 0);
    @Mock
    private ConversationJpaRepository conversationJpaRepository;
    @InjectMocks
    private ConversationRepositoryImpl conversationRepositoryImpl;

    private Conversation buildConversation() {
        return new Conversation(conversationId, "user-1", "title", "system message", createdAt, lastMessageAt);
    }

    private ConversationEntity buildEntity() {
        ConversationEntity entity = new ConversationEntity();
        entity.setId(conversationId);
        entity.setUserId("user-1");
        entity.setTitle("title");
        entity.setSystemMessage("system message");
        entity.setCreatedAt(createdAt);
        entity.setLastMessageAt(lastMessageAt);
        return entity;
    }

    @Test
    void save_shouldPersistEntityAndReturnMappedConversation() {
        // Given
        Conversation conversation = buildConversation();
        when(conversationJpaRepository.save(any(ConversationEntity.class))).thenReturn(buildEntity());

        // When
        Conversation saved = conversationRepositoryImpl.save(conversation);

        // Then
        ArgumentCaptor<ConversationEntity> captor = ArgumentCaptor.forClass(ConversationEntity.class);
        verify(conversationJpaRepository).save(captor.capture());
        ConversationEntity persistedEntity = captor.getValue();
        assertThat(persistedEntity.getId()).isEqualTo(conversationId);
        assertThat(persistedEntity.getUserId()).isEqualTo("user-1");
        assertThat(persistedEntity.getTitle()).isEqualTo("title");
        assertThat(persistedEntity.getSystemMessage()).isEqualTo("system message");
        assertThat(persistedEntity.getCreatedAt()).isEqualTo(createdAt);
        assertThat(persistedEntity.getLastMessageAt()).isEqualTo(lastMessageAt);

        assertThat(saved.getId()).isEqualTo(conversationId);
        assertThat(saved.getUserId()).isEqualTo("user-1");
        assertThat(saved.getTitle()).isEqualTo("title");
        assertThat(saved.getSystemMessage()).isEqualTo("system message");
        assertThat(saved.getCreatedAt()).isEqualTo(createdAt);
        assertThat(saved.getLastMessageAt()).isEqualTo(lastMessageAt);
    }

    @Test
    void updateLastMessageAtToNow_shouldPersistEntityWithUpdatedLastMessageAt() {
        // Given
        Conversation conversation = buildConversation();
        when(conversationJpaRepository.save(any(ConversationEntity.class))).thenReturn(buildEntity());

        // When
        conversationRepositoryImpl.updateLastMessageAtToNow(conversation);

        // Then
        ArgumentCaptor<ConversationEntity> captor = ArgumentCaptor.forClass(ConversationEntity.class);
        verify(conversationJpaRepository).save(captor.capture());
        assertThat(captor.getValue().getLastMessageAt()).isAfter(lastMessageAt);
    }

    @Test
    void findByIdAndUserId_shouldReturnMappedConversation_whenFound() {
        // Given
        when(conversationJpaRepository.findByIdAndUserId(conversationId, "user-1")).thenReturn(Optional.of(buildEntity()));

        // When
        Optional<Conversation> result = conversationRepositoryImpl.findByIdAndUserId(conversationId, "user-1");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(conversationId);
    }

    @Test
    void findByIdAndUserId_shouldReturnEmpty_whenNotFound() {
        // Given
        when(conversationJpaRepository.findByIdAndUserId(conversationId, "user-1")).thenReturn(Optional.empty());

        // When
        Optional<Conversation> result = conversationRepositoryImpl.findByIdAndUserId(conversationId, "user-1");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findAllByUserId_shouldReturnMappedConversations() {
        // Given
        when(conversationJpaRepository.findAllByUserId("user-1")).thenReturn(List.of(buildEntity()));

        // When
        List<Conversation> result = conversationRepositoryImpl.findAllByUserId("user-1");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(conversationId);
    }

    @Test
    void findAll_shouldReturnMappedConversations() {
        // Given
        when(conversationJpaRepository.findAll()).thenReturn(List.of(buildEntity()));

        // When
        List<Conversation> result = conversationRepositoryImpl.findAll();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(conversationId);
    }

    @Test
    void delete_shouldDelegateToJpaRepository() {
        // Given
        List<UUID> ids = List.of(conversationId);

        // When
        conversationRepositoryImpl.delete(ids);

        // Then
        verify(conversationJpaRepository).deleteAllById(ids);
    }
}
