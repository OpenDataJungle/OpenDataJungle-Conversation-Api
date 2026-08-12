package com.opendatajungle.conversation.api.infra.service.chatpreprocessor;

import com.opendatajungle.conversation.api.infra.model.ChatContext;

public interface ChatPreProcessingOrchestrator {
    ChatContext run(ChatContext initial);
}
