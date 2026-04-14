package com.laulem.vectopathappapi.infra.repository;

import com.laulem.vectopathappapi.business.model.Conversation;
import com.laulem.vectopathappapi.business.service.ConversationRepository;
import com.laulem.vectopathappapi.infra.entity.ConversationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ConversationRepositoryImpl implements ConversationRepository {
    private final ConversationJpaRepository conversationJpaRepository;

    @Override
    public Conversation save(Conversation conversation) {
        ConversationEntity entity = toConversationEntity(conversation);
        return toConversation(conversationJpaRepository.save(entity));
    }

    @Override
    public void updateLastMessageAtToNow(Conversation conversation) {
        ConversationEntity entity = toConversationEntity(conversation);
        entity.setLastMessageAt(LocalDateTime.now());
        conversationJpaRepository.save(entity);
    }

    @Override
    public Optional<Conversation> findByIdAndUserId(UUID conversationId, String userId) {
        return conversationJpaRepository.findByIdAndUserId(conversationId, userId).map(this::toConversation);
    }

    @Override
    public List<Conversation> findAllByUserId(String userId) {
        return conversationJpaRepository.findAllByUserId(userId).stream()
                .map(this::toConversation)
                .toList();
    }

    @Override
    public List<Conversation> findAll() {
        return conversationJpaRepository.findAll().stream()
                .map(this::toConversation)
                .toList();
    }

    @Override
    public void delete(List<UUID> conversationIdsToDelete) {
        conversationJpaRepository.deleteAllById(conversationIdsToDelete);
    }

    private ConversationEntity toConversationEntity(Conversation conversation) {
        ConversationEntity conversationEntity = new ConversationEntity();
        conversationEntity.setId(conversation.getId());
        conversationEntity.setUserId(conversation.getUserId());
        conversationEntity.setTitle(conversation.getTitle());
        conversationEntity.setSystemMessage(conversation.getSystemMessage());
        conversationEntity.setCreatedAt(conversation.getCreatedAt());
        conversationEntity.setLastMessageAt(conversation.getLastMessageAt());
        return conversationEntity;
    }

    private Conversation toConversation(ConversationEntity entity) {
        return new Conversation(
                entity.getId(),
                entity.getUserId(),
                entity.getTitle(),
                entity.getSystemMessage(),
                entity.getCreatedAt(),
                entity.getLastMessageAt()
        );
    }
}
