package com.laulem.vectopathappapi.business.service;

import com.laulem.vectopathappapi.business.model.SendChatMessageCommand;
import com.laulem.vectopathappapi.business.model.ChatResult;

public interface ChatService {
    ChatResult chat(SendChatMessageCommand sendChatMessageCommand, String systemMessage);
}


