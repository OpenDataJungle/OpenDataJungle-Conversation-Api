package com.laulem.vectopathappapi.infra.service.chatpreprocessor;

import com.laulem.vectopathappapi.infra.model.ChatContext;

public interface ChatPreProcessor {
    ChatContext process(ChatContext context);

    default int getOrder() {
        return 0;
    }
}
