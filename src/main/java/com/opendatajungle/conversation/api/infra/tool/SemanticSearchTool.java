package com.opendatajungle.conversation.api.infra.tool;

import com.opendatajungle.conversation.api.infra.model.Search;
import com.opendatajungle.conversation.api.infra.properties.ChatProperties;
import com.opendatajungle.conversation.api.infra.service.SearchService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class SemanticSearchTool {

    private final SearchService searchService;
    private final ChatRequestHolder chatRequestHolder;
    private final ChatProperties chatProperties;

    public SemanticSearchTool(SearchService searchService,
                              ChatRequestHolder chatRequestHolder,
                              ChatProperties chatProperties) {
        this.searchService = searchService;
        this.chatRequestHolder = chatRequestHolder;
        this.chatProperties = chatProperties;
    }

    @Tool(description = "Search resources content for answering the user question")
    public List<Search> searchResources(String query) {
        List<UUID> resourceIds = chatRequestHolder.getResourceIds();
        List<Search> results = searchService.search(query, chatProperties.maxInternalResourceSearchResults(), resourceIds);
        chatRequestHolder.addToolResult("searchResources", query, Map.of("data", results));
        return results;
    }
}
