package com.laulem.vectopath.conversation.api.infra.service.factory;

import com.laulem.vectopath.conversation.api.infra.properties.LlmModelConfig;
import org.springframework.ai.chat.client.ChatClient;

public interface ChatClientFactory {
    boolean supports(String provider);

    ChatClient build(LlmModelConfig config);
}
