package com.laulem.vectopath.conversation.api.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.laulem.vectopath.conversation.api.shared.validation.SizeType;
import com.laulem.vectopath.conversation.api.shared.validation.ValidSizeByType;

public record UpdateConversationRequest(
        @JsonProperty("title")
        @ValidSizeByType(SizeType.TITLE)
        String title,

        @JsonProperty("system_message")
        @ValidSizeByType(SizeType.SYSTEM_MESSAGE)
        String systemMessage) {
}
