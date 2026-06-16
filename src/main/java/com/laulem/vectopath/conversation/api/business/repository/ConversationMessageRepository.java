package com.laulem.vectopath.conversation.api.business.repository;

import com.laulem.vectopath.conversation.api.business.model.ConversationMessage;

import java.util.List;
import java.util.UUID;

public interface ConversationMessageRepository {
    List<ConversationMessage> findAllByConversationId(UUID conversationId);
}
