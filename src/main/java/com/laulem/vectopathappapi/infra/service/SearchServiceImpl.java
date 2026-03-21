package com.laulem.vectopathappapi.infra.service;

import com.laulem.vectopathappapi.business.model.Search;
import com.laulem.vectopathappapi.business.service.AuthenticationService;
import com.laulem.vectopathappapi.business.service.SearchService;
import com.laulem.vectopathappapi.infra.dto.SearchRequest;
import com.laulem.vectopathappapi.infra.dto.SearchResponse;
import com.laulem.vectopathappapi.infra.properties.VectoPathApiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class SearchServiceImpl implements SearchService {
    private final RestClient restClient;
    private final AuthenticationService authenticationService;
    private final String searchSemanticPath;

    public SearchServiceImpl(RestClient.Builder restClientBuilder,
                             VectoPathApiProperties vectoPathApiProperties,
                             AuthenticationService authenticationService) {
        this.restClient = restClientBuilder.baseUrl(vectoPathApiProperties.getBaseUrl()).build();
        this.authenticationService = authenticationService;
        this.searchSemanticPath = vectoPathApiProperties.getSearchSemanticPath();
    }

    @Override
    public List<Search> search(String query, int limit) {
        String bearerToken = authenticationService.getToken()
                .orElseThrow(() -> new IllegalStateException("No Bearer token available in security context"));

        SearchRequest request = new SearchRequest(query, limit, 0.5);

        try {
            List<SearchResponse> searchResponse = restClient.post()
                    .uri(searchSemanticPath)
                    .header("Authorization", "Bearer " + bearerToken)
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            return searchResponse.stream().map(SearchResponse::toSearch).toList();
        } catch (HttpClientErrorException e) {
            log.error("Error during search request: {} - {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            return Collections.emptyList();
        }
    }
}

