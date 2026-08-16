package com.opendatajungle.conversation.api.infra.service.chatpreprocessor;

import com.opendatajungle.conversation.api.infra.model.ChatContext;

/**
 * Interface for orchestrating the execution of multiple ChatPreProcessor instances.
 * It defines a method to run the processors sequentially on a given ChatContext.
 */
public interface ChatPreProcessingOrchestrator {
    ChatContext run(ChatContext initial);
}
