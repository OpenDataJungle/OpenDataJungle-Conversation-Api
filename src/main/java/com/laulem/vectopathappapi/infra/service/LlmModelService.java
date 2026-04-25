package com.laulem.vectopathappapi.infra.service;

import org.springframework.ai.chat.client.ChatClient;

public interface LlmModelService {
    ChatClient getDefaultModel();

    boolean hasModel(String modelId);

    ChatClient getModel(LlmModelKey key);

    ChatClient getModel(String name);

    boolean hasSpeedModel();

    boolean hasCategorizerModel();

    boolean hasLongContextModel();
}
