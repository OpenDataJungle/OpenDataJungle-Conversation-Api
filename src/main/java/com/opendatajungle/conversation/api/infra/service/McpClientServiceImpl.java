package com.opendatajungle.conversation.api.infra.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opendatajungle.conversation.api.infra.properties.McpProperties;
import com.opendatajungle.conversation.api.infra.properties.McpServerConfig;
import com.opendatajungle.conversation.api.infra.tool.ChatRequestHolder;
import com.opendatajungle.conversation.api.infra.tool.McpToolCallbackWrapper;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.SyncMcpToolCallback;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class McpClientServiceImpl implements McpClientService {
    private static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(30);

    private final Map<String, McpServerConfig> serverConfigs;
    private final Map<String, McpSyncClient> syncClients;
    private final Map<String, List<ToolCallback>> allToolCallbacksByServer;
    private final List<ToolCallback> requiredToolCallbacks;
    private final Map<String, List<ToolCallback>> optionalToolCallbacks;
    private final ChatRequestHolder chatRequestHolder;

    public McpClientServiceImpl(McpProperties mcpProperties, ObjectMapper objectMapper, ChatRequestHolder chatRequestHolder) {
        this.chatRequestHolder = chatRequestHolder;
        if (!StringUtils.hasText(mcpProperties.serverJson())) {
            this.serverConfigs = Collections.emptyMap();
            this.syncClients = Collections.emptyMap();
            this.allToolCallbacksByServer = Collections.emptyMap();
            this.requiredToolCallbacks = Collections.emptyList();
            this.optionalToolCallbacks = Collections.emptyMap();
            return;
        }

        try {
            this.serverConfigs = Collections.unmodifiableMap(objectMapper.readValue(mcpProperties.serverJson(), new TypeReference<>() {
            }));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse OPEN_DATA_JUNGLE_MCP_SERVERS JSON. Check the environment variable format.", e);
        }

        this.syncClients = buildAndInitClients(this.serverConfigs);
        this.allToolCallbacksByServer = buildAllToolCallbacks();
        this.requiredToolCallbacks = buildRequiredToolCallbacks();
        this.optionalToolCallbacks = buildOptionalToolCallbacks();
    }

    private List<ToolCallback> buildRequiredToolCallbacks() {
        return allToolCallbacksByServer.entrySet().stream().filter(e -> {
            McpServerConfig cfg = serverConfigs.get(e.getKey());
            return cfg != null && cfg.isRequired();
        }).flatMap(e -> e.getValue().stream()).toList();
    }

    private Map<String, List<ToolCallback>> buildOptionalToolCallbacks() {
        Map<String, List<ToolCallback>> result = new HashMap<>();
        allToolCallbacksByServer.forEach((key, callbacks) -> {
            McpServerConfig cfg = serverConfigs.get(key);
            if (cfg != null && !cfg.isRequired()) {
                result.put(cfg.getName(), callbacks);
            }
        });
        return Collections.unmodifiableMap(result);
    }

    @Override
    public ToolCallback[] getRequiredToolCallbacks() {
        return requiredToolCallbacks.toArray(ToolCallback[]::new);
    }

    @Override
    public ToolCallback[] getRequiredToolCallbacksWithAdditional(Set<String> enabledTools) {
        if (CollectionUtils.isEmpty(enabledTools)) {
            return getRequiredToolCallbacks();
        }

        List<ToolCallback> result = new ArrayList<>(requiredToolCallbacks);
        enabledTools.stream()
                .map(optionalToolCallbacks::get)
                .filter(callbacks -> !CollectionUtils.isEmpty(callbacks))
                .forEach(result::addAll);
        return result.toArray(ToolCallback[]::new);
    }

    @PreDestroy
    public void destroy() {
        log.info("Closing {} MCP client(s)...", syncClients.size());
        syncClients.forEach((key, client) -> {
            try {
                client.close();
                log.debug("MCP client '{}' closed.", key);
            } catch (Exception e) {
                log.warn("Error while closing MCP client '{}': {}", key, e.getMessage());
            }
        });
    }

    private Map<String, McpSyncClient> buildAndInitClients(Map<String, McpServerConfig> configs) {
        Map<String, McpSyncClient> clients = new HashMap<>();
        for (Map.Entry<String, McpServerConfig> entry : configs.entrySet()) {
            String key = entry.getKey();
            McpServerConfig config = entry.getValue();
            try {
                McpSyncClient client = buildClient(key, config);
                clients.put(key, client);
            } catch (Exception e) {
                log.error("Failed to initialize MCP server '{}' ({}): {}. It will be skipped.", key, config.getUrl(), e.getMessage());
            }
        }
        return Collections.unmodifiableMap(clients);
    }

    private McpSyncClient buildClient(String key, McpServerConfig config) {
        McpClientTransport transport = buildTransport(key, config);
        McpSyncClient client = McpClient.sync(transport).requestTimeout(DEFAULT_REQUEST_TIMEOUT).build();
        client.initialize();
        return client;
    }

    private McpClientTransport buildTransport(String key, McpServerConfig config) {
        if (!StringUtils.hasText(config.getType())) {
            throw new IllegalArgumentException("MCP server '" + key + "' is missing required field 'type' (expected: 'sse' or 'http').");
        }
        if (!StringUtils.hasText(config.getUrl())) {
            throw new IllegalArgumentException("MCP server '" + key + "' is missing required field 'url'.");
        }

        return switch (config.getType().toLowerCase()) {
            case "sse" -> {
                var builder = HttpClientSseClientTransport.builder(config.getUrl());
                if (!CollectionUtils.isEmpty(config.getHeaders())) {
                    builder.customizeRequest(r -> config.getHeaders().forEach(r::header));
                }
                yield builder.build();
            }
            case "http" -> {
                var builder = HttpClientStreamableHttpTransport.builder(config.getUrl());
                if (!CollectionUtils.isEmpty(config.getHeaders())) {
                    builder.customizeRequest(r -> config.getHeaders().forEach(r::header));
                }
                yield builder.build();
            }
            default ->
                    throw new IllegalArgumentException("Unsupported MCP transport type for server '" + key + "': '" + config.getType() + "'. Supported values: 'sse', 'http'.");
        };
    }

    private Map<String, List<ToolCallback>> buildAllToolCallbacks() {
        Map<String, List<ToolCallback>> result = new HashMap<>();
        for (Map.Entry<String, McpSyncClient> entry : syncClients.entrySet()) {
            String key = entry.getKey();
            try {
                var listResult = entry.getValue().listTools(null);
                if (listResult == null || CollectionUtils.isEmpty(listResult.tools())) {
                    result.put(key, Collections.emptyList());
                    continue;
                }
                List<ToolCallback> callbacks = listResult.tools().stream()
                        .map(tool -> (ToolCallback) new McpToolCallbackWrapper(
                                SyncMcpToolCallback.builder().mcpClient(entry.getValue()).tool(tool).build(),
                                chatRequestHolder
                        ))
                        .toList();
                result.put(key, callbacks);
            } catch (Exception e) {
                log.error("Failed to retrieve tools from MCP server '{}': {}", key, e.getMessage());
                result.put(key, Collections.emptyList());
            }
        }
        return Collections.unmodifiableMap(result);
    }
}
