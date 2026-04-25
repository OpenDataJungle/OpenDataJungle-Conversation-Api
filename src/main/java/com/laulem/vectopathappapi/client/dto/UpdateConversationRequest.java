package com.laulem.vectopathappapi.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.laulem.vectopathappapi.shared.validation.SizeType;
import com.laulem.vectopathappapi.shared.validation.ValidSizeByType;

public record UpdateConversationRequest(
        @JsonProperty("title")
        @ValidSizeByType(SizeType.TITLE)
        String title,

        @JsonProperty("system_message")
        @ValidSizeByType(SizeType.SYSTEM_MESSAGE)
        String systemMessage) {
}
