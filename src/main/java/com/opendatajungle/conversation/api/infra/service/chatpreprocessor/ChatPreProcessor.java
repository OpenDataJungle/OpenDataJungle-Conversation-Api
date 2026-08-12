package com.opendatajungle.conversation.api.infra.service.chatpreprocessor;

import com.opendatajungle.conversation.api.infra.model.ChatContext;

public interface ChatPreProcessor {
    ChatContext process(ChatContext context);

    default int getOrder() {
        return 0;
    }
}
