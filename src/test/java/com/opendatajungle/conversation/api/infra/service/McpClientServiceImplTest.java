package com.opendatajungle.conversation.api.infra.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opendatajungle.conversation.api.infra.properties.McpProperties;
import com.opendatajungle.conversation.api.infra.tool.ChatRequestHolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class McpClientServiceImplTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    private ChatRequestHolder chatRequestHolder;

    @Test
    void constructor_shouldResultInEmptyToolCallbacks_whenServerJsonIsBlank() {
        // Given
        McpProperties mcpProperties = new McpProperties("   ");

        // When
        McpClientServiceImpl service = new McpClientServiceImpl(mcpProperties, objectMapper, chatRequestHolder);

        // Then
        assertThat(service.getRequiredToolCallbacks()).isEmpty();
    }

    @Test
    void constructor_shouldResultInEmptyToolCallbacks_whenServerJsonIsNull() {
        // Given
        McpProperties mcpProperties = new McpProperties(null);

        // When
        McpClientServiceImpl service = new McpClientServiceImpl(mcpProperties, objectMapper, chatRequestHolder);

        // Then
        assertThat(service.getRequiredToolCallbacks()).isEmpty();
    }

    @Test
    void constructor_shouldThrowIllegalStateException_whenServerJsonIsMalformed() {
        // Given
        McpProperties mcpProperties = new McpProperties("not-json");

        // When & Then
        assertThatThrownBy(() -> new McpClientServiceImpl(mcpProperties, objectMapper, chatRequestHolder))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to parse OPEN_DATA_JUNGLE_MCP_SERVERS JSON");
    }

    @Test
    void constructor_shouldSkipServer_whenTypeIsMissing() {
        // Given
        McpProperties mcpProperties = new McpProperties(
                "{\"server-a\":{\"name\":\"server-a\",\"url\":\"http://localhost:8080\",\"required\":true}}");

        // When
        McpClientServiceImpl service = new McpClientServiceImpl(mcpProperties, objectMapper, chatRequestHolder);

        // Then
        assertThat(service.getRequiredToolCallbacks()).isEmpty();
    }

    @Test
    void constructor_shouldSkipServer_whenUrlIsMissing() {
        // Given
        McpProperties mcpProperties = new McpProperties(
                "{\"server-a\":{\"name\":\"server-a\",\"type\":\"sse\",\"required\":true}}");

        // When
        McpClientServiceImpl service = new McpClientServiceImpl(mcpProperties, objectMapper, chatRequestHolder);

        // Then
        assertThat(service.getRequiredToolCallbacks()).isEmpty();
    }

    @Test
    void constructor_shouldSkipServer_whenTypeIsUnsupported() {
        // Given
        McpProperties mcpProperties = new McpProperties(
                "{\"server-a\":{\"name\":\"server-a\",\"type\":\"ftp\",\"url\":\"http://localhost:8080\",\"required\":true}}");

        // When
        McpClientServiceImpl service = new McpClientServiceImpl(mcpProperties, objectMapper, chatRequestHolder);

        // Then
        assertThat(service.getRequiredToolCallbacks()).isEmpty();
    }

    // TODO : getRequiredToolCallbacksWithAdditional_shouldReturnRequiredOnly_whenEnabledToolsIsNull
    // TODO : getRequiredToolCallbacksWithAdditional_shouldReturnRequiredOnly_whenEnabledToolsIsEmpty

    @Test
    void destroy_shouldNotThrow_whenNoClientsInitialized() {
        // Given
        McpProperties mcpProperties = new McpProperties(null);
        McpClientServiceImpl service = new McpClientServiceImpl(mcpProperties, objectMapper, chatRequestHolder);

        // When & Then
        assertThatCode(service::destroy).doesNotThrowAnyException();
    }
}
