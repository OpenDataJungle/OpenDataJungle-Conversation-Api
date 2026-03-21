package com.laulem.vectopathappapi.business.service;

import com.laulem.vectopathappapi.business.model.ChatResult;
import com.laulem.vectopathappapi.business.model.Conversation;
import com.laulem.vectopathappapi.business.model.ConversationMessage;

import java.util.List;
import java.util.UUID;

public interface ConversationService {
    Conversation create(String title, String systemMessage);

    Conversation findById(UUID id);

    List<Conversation> findAllByUser();

    List<Conversation> findAll();

    Conversation update(UUID id, String title, String systemMessage);

    void deleteByIds(List<UUID> ids);

    List<ConversationMessage> getMessages(UUID conversationId);

    ChatResult chat(UUID conversationId, String message);
}
