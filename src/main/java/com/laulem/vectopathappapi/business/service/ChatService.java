package com.laulem.vectopathappapi.business.service;

import com.laulem.vectopathappapi.business.model.ChatResult;

import java.util.List;
import java.util.UUID;

public interface ChatService {
    ChatResult chat(UUID conversationId, String systemMessage, String message, List<UUID> resourceIds);
}


