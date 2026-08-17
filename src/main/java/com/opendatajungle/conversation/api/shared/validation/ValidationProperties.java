package com.opendatajungle.conversation.api.shared.validation;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "open-data-jungle.validation")
public record ValidationProperties(
        int messageMaxSize,
        int systemMessageMaxSize,
        int titleMaxSize,
        int resourceIdsMaxSize,
        int enabledToolsMaxSize) {

    public ValidationProperties {
        if (messageMaxSize <= 0) messageMaxSize = 50_000;
        if (systemMessageMaxSize <= 0) systemMessageMaxSize = 10_000;
        if (titleMaxSize <= 0) titleMaxSize = 500;
        if (resourceIdsMaxSize <= 0) resourceIdsMaxSize = 10;
        if (enabledToolsMaxSize <= 0) enabledToolsMaxSize = 10;
    }

    public int maxSizeFor(SizeType sizeType) {
        return switch (sizeType) {
            case MESSAGE -> messageMaxSize;
            case SYSTEM_MESSAGE -> systemMessageMaxSize;
            case TITLE -> titleMaxSize;
            case RESOURCE_IDS -> resourceIdsMaxSize;
            case ENABLED_TOOLS -> enabledToolsMaxSize;
        };
    }
}
