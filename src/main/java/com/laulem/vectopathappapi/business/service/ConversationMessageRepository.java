package com.laulem.vectopathappapi.business.service;

import com.laulem.vectopathappapi.business.model.ConversationMessage;

import java.util.List;
import java.util.UUID;

public interface ConversationMessageRepository {
    List<ConversationMessage> findAllByConversationId(UUID conversationId);
}
