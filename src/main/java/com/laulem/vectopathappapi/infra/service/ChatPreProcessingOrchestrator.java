package com.laulem.vectopathappapi.infra.service;

import com.laulem.vectopathappapi.infra.model.ChatContext;

public interface ChatPreProcessingOrchestrator {
    ChatContext run(ChatContext initial);
}
