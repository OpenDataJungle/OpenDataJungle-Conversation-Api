package com.opendatajungle.conversation.api.infra.service;

import org.springframework.ai.chat.client.ChatClient;

/**
 * Service interface for managing LLM (Large Language Model) clients.
 */
public interface LlmModelService {
    ChatClient getDefaultModel();

    boolean hasModel(String modelId);

    ChatClient getModel(LlmModelKey key);

    ChatClient getModel(String name);

    boolean hasSpeedModel();

    boolean hasCategorizerModel();

    boolean hasLongContextModel();
}
