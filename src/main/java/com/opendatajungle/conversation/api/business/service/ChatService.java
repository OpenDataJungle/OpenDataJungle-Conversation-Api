package com.opendatajungle.conversation.api.business.service;

import com.opendatajungle.conversation.api.business.model.ChatResult;
import com.opendatajungle.conversation.api.business.model.SendChatMessageCommand;

public interface ChatService {
    ChatResult chat(SendChatMessageCommand sendChatMessageCommand, String systemMessage);
}


