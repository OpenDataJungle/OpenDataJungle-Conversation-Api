package com.laulem.vectopath.conversation.api.business.service;

import com.laulem.vectopath.conversation.api.business.model.SendChatMessageCommand;
import com.laulem.vectopath.conversation.api.business.model.ChatResult;

public interface ChatService {
    ChatResult chat(SendChatMessageCommand sendChatMessageCommand, String systemMessage);
}


