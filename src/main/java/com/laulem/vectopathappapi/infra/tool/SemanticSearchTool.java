package com.laulem.vectopathappapi.infra.tool;

import com.laulem.vectopathappapi.business.model.Search;
import com.laulem.vectopathappapi.business.service.SearchService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class SemanticSearchTool {

    public static final int SEARCH_LIMIT = 3;
    private final SearchService searchService;
    private final ChatRequestHolder chatRequestHolder;

    public SemanticSearchTool(SearchService searchService,
                              ChatRequestHolder chatRequestHolder) {
        this.searchService = searchService;
        this.chatRequestHolder = chatRequestHolder;
    }

    @Tool(description = "Search resources content for answering the user question")
    public List<Search> searchResources(String userQuestion) {
        List<UUID> resourceIds = chatRequestHolder.getResourceIds();
        List<Search> results = searchService.search(userQuestion, SEARCH_LIMIT, resourceIds);
        chatRequestHolder.addToolResult("searchResources", Map.of("data", results));
        return results;
    }
}
