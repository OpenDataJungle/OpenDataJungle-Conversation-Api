package com.laulem.vectopath.conversation.api.business.repository;

import com.laulem.vectopath.conversation.api.business.model.Conversation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository {
    Conversation save(Conversation conversation);

    void updateLastMessageAtToNow(Conversation conversation);

    Optional<Conversation> findByIdAndUserId(UUID conversationId, String userId);

    List<Conversation> findAllByUserId(String userId);

    List<Conversation> findAll();

    void delete(List<UUID> conversationIdsToDelete);
}
