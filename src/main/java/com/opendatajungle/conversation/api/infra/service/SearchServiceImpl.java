package com.opendatajungle.conversation.api.infra.service;

import com.opendatajungle.conversation.api.business.service.AuthenticationService;
import com.opendatajungle.conversation.api.infra.model.Search;
import com.opendatajungle.conversation.api.infra.dto.SearchRequest;
import com.opendatajungle.conversation.api.infra.dto.SearchResponse;
import com.opendatajungle.conversation.api.infra.properties.OpenDataJungleKnowledgeApiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
public class SearchServiceImpl implements SearchService {
    private final RestClient restClient;
    private final AuthenticationService authenticationService;
    private final String searchSemanticPath;

    public SearchServiceImpl(RestClient.Builder restClientBuilder,
                             OpenDataJungleKnowledgeApiProperties openDataJungleKnowledgeApiProperties,
                             AuthenticationService authenticationService) {
        this.restClient = restClientBuilder.baseUrl(openDataJungleKnowledgeApiProperties.baseUrl()).build();
        this.authenticationService = authenticationService;
        this.searchSemanticPath = openDataJungleKnowledgeApiProperties.searchSemanticPath();
    }

    @Override
    public List<Search> search(String query, int limit, List<UUID> resourceIds) {
        String bearerToken = authenticationService.getToken().orElse("");
        SearchRequest request = new SearchRequest(query, limit, 0.5, resourceIds);

        try {
            List<SearchResponse> searchResponse = restClient.post()
                    .uri(searchSemanticPath)
                    .header("Authorization", "Bearer " + bearerToken)
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            if (searchResponse == null) return Collections.emptyList();
            return searchResponse.stream().map(SearchResponse::toSearch).toList();
        } catch (HttpClientErrorException e) {
            log.error("Error during search request: {} - {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            return Collections.emptyList();
        }
    }
}
