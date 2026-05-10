package com.laulem.vectopathappapi.infra.service.factory;

import com.laulem.vectopathappapi.infra.properties.LlmModelConfig;
import org.springframework.ai.chat.client.ChatClient;

public interface ChatClientFactory {
    boolean supports(String provider);

    ChatClient build(LlmModelConfig config);
}
