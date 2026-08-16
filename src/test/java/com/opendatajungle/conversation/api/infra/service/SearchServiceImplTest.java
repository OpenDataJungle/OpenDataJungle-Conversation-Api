package com.opendatajungle.conversation.api.infra.service;

import com.opendatajungle.commons.business.service.AuthenticationUseCase;
import com.opendatajungle.conversation.api.infra.dto.SearchRequest;
import com.opendatajungle.conversation.api.infra.dto.SearchResponse;
import com.opendatajungle.conversation.api.infra.model.Search;
import com.opendatajungle.conversation.api.infra.properties.OpenDataJungleKnowledgeApiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceImplTest {

    @Mock
    private RestClient.Builder restClientBuilder;

    @Mock
    private RestClient restClient;

    @Mock
    private AuthenticationUseCase authenticationService;

    @Mock
    private RestClient.RequestBodyUriSpec bodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec bodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private SearchServiceImpl searchServiceImpl;

    private final OpenDataJungleKnowledgeApiProperties properties = new OpenDataJungleKnowledgeApiProperties(
            "https://laulem.com", "/api/v1/search", "/api/v1/resources");

    @BeforeEach
    void setUp() {
        when(restClientBuilder.baseUrl(properties.baseUrl())).thenReturn(restClientBuilder);
        when(restClientBuilder.build()).thenReturn(restClient);
        searchServiceImpl = new SearchServiceImpl(restClientBuilder, properties, authenticationService);
    }

    @Test
    void search_shouldReturnMappedResults_whenRequestSucceeds() {
        // Given
        UUID resourceId = UUID.randomUUID();
        UUID vectorId = UUID.randomUUID();
        when(authenticationService.getToken()).thenReturn(Optional.of("token-value"));
        when(restClient.post()).thenReturn(bodyUriSpec);
        when(bodyUriSpec.uri(properties.searchSemanticPath())).thenReturn(bodySpec);
        when(bodySpec.header(eq("Authorization"), anyString())).thenReturn(bodySpec);
        when(bodySpec.body(any(SearchRequest.class))).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        SearchResponse searchResponse = new SearchResponse(
                vectorId, resourceId, "resource", "content", "text/plain", "metadata", 0.9, null, null);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(List.of(searchResponse));

        // When
        List<Search> result = searchServiceImpl.search("query", 5, List.of(resourceId));

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().vectorId()).isEqualTo(vectorId);
        assertThat(result.getFirst().resourceId()).isEqualTo(resourceId);
    }

    @Test
    void search_shouldReturnEmptyList_whenResponseBodyIsNull() {
        // Given
        when(authenticationService.getToken()).thenReturn(Optional.of("token-value"));
        when(restClient.post()).thenReturn(bodyUriSpec);
        when(bodyUriSpec.uri(properties.searchSemanticPath())).thenReturn(bodySpec);
        when(bodySpec.header(eq("Authorization"), anyString())).thenReturn(bodySpec);
        when(bodySpec.body(any(SearchRequest.class))).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(null);

        // When
        List<Search> result = searchServiceImpl.search("query", 5, List.of());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void search_shouldUseEmptyBearerToken_whenNoTokenAvailable() {
        // Given
        UUID resourceId = UUID.randomUUID();
        UUID vectorId = UUID.randomUUID();
        when(authenticationService.getToken()).thenReturn(Optional.of(""));
        when(restClient.post()).thenReturn(bodyUriSpec);
        when(bodyUriSpec.uri(properties.searchSemanticPath())).thenReturn(bodySpec);
        when(bodySpec.header("Authorization", "Bearer ")).thenReturn(bodySpec);
        when(bodySpec.body(any(SearchRequest.class))).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        SearchResponse searchResponse = new SearchResponse(
                vectorId, resourceId, "resource", "content", "text/plain", "metadata", 0.9, null, null);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(List.of(searchResponse));

        // When
        List<Search> result = searchServiceImpl.search("query", 5, List.of(resourceId));

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().vectorId()).isEqualTo(vectorId);
        assertThat(result.getFirst().resourceId()).isEqualTo(resourceId);
    }

    @Test
    void search_shouldReturnEmptyList_whenHttpClientErrorExceptionThrown() {
        // Given
        when(authenticationService.getToken()).thenReturn(Optional.of("token-value"));
        when(restClient.post()).thenReturn(bodyUriSpec);
        when(bodyUriSpec.uri(properties.searchSemanticPath())).thenReturn(bodySpec);
        when(bodySpec.header(eq("Authorization"), anyString())).thenReturn(bodySpec);
        when(bodySpec.body(any(SearchRequest.class))).thenReturn(bodySpec);
        when(bodySpec.retrieve())
                .thenThrow(HttpClientErrorException.create(HttpStatusCode.valueOf(500), "Server Error", null, null, null));

        // When
        List<Search> result = searchServiceImpl.search("query", 5, List.of());

        // Then
        assertThat(result).isEmpty();
    }
}
