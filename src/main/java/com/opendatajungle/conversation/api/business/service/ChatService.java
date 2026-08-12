package com.opendatajungle.conversation.api.business.service;

import com.opendatajungle.conversation.api.business.model.SendChatMessageCommand;
import com.opendatajungle.conversation.api.business.model.ChatResult;

public interface ChatService {
    ChatResult chat(SendChatMessageCommand sendChatMessageCommand, String systemMessage);
}


