package com.laulem.vectopathappapi.business.service;

import com.laulem.vectopathappapi.business.model.ConversationMessage;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConversationMessageRepository {
    List<ConversationMessage> findAllByConversationId(UUID conversationId);
}
