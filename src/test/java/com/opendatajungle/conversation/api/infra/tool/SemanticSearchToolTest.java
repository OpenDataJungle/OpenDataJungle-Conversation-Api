package com.opendatajungle.conversation.api.infra.tool;

import com.opendatajungle.conversation.api.infra.model.Search;
import com.opendatajungle.conversation.api.infra.service.SearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SemanticSearchToolTest {

    @Mock
    private SearchService searchService;

    @Mock
    private ChatRequestHolder chatRequestHolder;

    @InjectMocks
    private SemanticSearchTool semanticSearchTool;

    @Test
    void searchResources_shouldDelegateToSearchServiceAndRecordToolResult() {
        // Given
        List<UUID> resourceIds = List.of(UUID.randomUUID());
        List<Search> searchResults = List.of(new Search(
                UUID.randomUUID(), UUID.randomUUID(), "resource", "content", "text/plain",
                "metadata", 0.8, null, null));
        when(chatRequestHolder.getResourceIds()).thenReturn(resourceIds);
        when(searchService.search("query", SemanticSearchTool.SEARCH_LIMIT, resourceIds)).thenReturn(searchResults);

        // When
        List<Search> result = semanticSearchTool.searchResources("query");

        // Then
        assertThat(result).isEqualTo(searchResults);
        verify(chatRequestHolder).addToolResult("searchResources", "query", Map.of("data", searchResults));
    }
}
