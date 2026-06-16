package com.laulem.vectopath.conversation.api.infra.properties;

import com.laulem.vectopath.conversation.api.infra.service.McpClientService;
import lombok.Data;

import java.util.Collections;
import java.util.Map;

/**
 * Loaded from {@link McpProperties} by {@link McpClientService}
 */
@Data
public class McpServerConfig {
    private String name;
    private String type;
    private String url;
    private boolean required = true;
    private Map<String, String> headers = Collections.emptyMap();
}
