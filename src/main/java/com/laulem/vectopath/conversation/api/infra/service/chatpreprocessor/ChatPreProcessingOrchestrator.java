package com.laulem.vectopath.conversation.api.infra.service.chatpreprocessor;

import com.laulem.vectopath.conversation.api.infra.model.ChatContext;

public interface ChatPreProcessingOrchestrator {
    ChatContext run(ChatContext initial);
}
