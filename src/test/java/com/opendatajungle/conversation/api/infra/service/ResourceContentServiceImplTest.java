package com.opendatajungle.conversation.api.infra.service;

import com.opendatajungle.commons.business.service.AuthenticationUseCase;
import com.opendatajungle.conversation.api.infra.dto.ResourceContentApiResponse;
import com.opendatajungle.conversation.api.infra.model.ResourceContent;
import com.opendatajungle.conversation.api.infra.properties.OpenDataJungleKnowledgeApiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class ResourceContentServiceImplTest {

    private final OpenDataJungleKnowledgeApiProperties properties = new OpenDataJungleKnowledgeApiProperties(
            "http://laulem.com", "/api/v1/search", "/api/v1/resources");
    @Mock
    private RestClient.Builder restClientBuilder;
    @Mock
    private RestClient restClient;
    @Mock
    private AuthenticationUseCase authenticationService;
    @Mock
    private RestClient.RequestHeadersUriSpec uriSpec;
    @Mock
    private RestClient.RequestHeadersSpec headersSpec;
    @Mock
    private RestClient.ResponseSpec responseSpec;
    private ResourceContentServiceImpl resourceContentServiceImpl;

    @BeforeEach
    void setUp() {
        when(restClientBuilder.baseUrl(properties.baseUrl())).thenReturn(restClientBuilder);
        when(restClientBuilder.build()).thenReturn(restClient);
        resourceContentServiceImpl = new ResourceContentServiceImpl(restClientBuilder, properties, authenticationService);
    }

    @Test
    void fetchContents_shouldReturnEmptyList_whenResourceIdsIsEmpty() {
        // Given & When
        List<ResourceContent> result = resourceContentServiceImpl.fetchContents(List.of());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void fetchContents_shouldReturnEmptyList_whenResourceIdsIsNull() {
        // Given & When
        List<ResourceContent> result = resourceContentServiceImpl.fetchContents(null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void fetchContents_shouldReturnMappedContent_whenRequestSucceeds() {
        // Given
        UUID id = UUID.randomUUID();
        when(authenticationService.getToken()).thenReturn(Optional.of("token-value"));
        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString(), any(Object.class))).thenReturn(headersSpec);
        when(headersSpec.header(eq("Authorization"), anyString())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(ResourceContentApiResponse.class))
                .thenReturn(new ResourceContentApiResponse(id, "file.txt", "content"));

        // When
        List<ResourceContent> result = resourceContentServiceImpl.fetchContents(List.of(id));

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(id);
        assertThat(result.getFirst().name()).isEqualTo("file.txt");
        assertThat(result.getFirst().content()).isEqualTo("content");
    }

    @Test
    void fetchContents_shouldUseEmptyBearerToken_whenNoTokenAvailable() {
        // Given
        UUID id = UUID.randomUUID();
        when(authenticationService.getToken()).thenReturn(Optional.empty());
        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString(), any(Object.class))).thenReturn(headersSpec);
        when(headersSpec.header("Authorization", "Bearer ")).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(ResourceContentApiResponse.class))
                .thenReturn(new ResourceContentApiResponse(id, "file.txt", "content"));

        // When
        List<ResourceContent> result = resourceContentServiceImpl.fetchContents(List.of(id));

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(id);
        assertThat(result.getFirst().name()).isEqualTo("file.txt");
        assertThat(result.getFirst().content()).isEqualTo("content");
    }

    @Test
    void fetchContents_shouldReturnEmpty_whenHttpClientErrorExceptionThrown() {
        // Given
        UUID id = UUID.randomUUID();
        when(authenticationService.getToken()).thenReturn(Optional.of("token-value"));
        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString(), any(Object.class))).thenReturn(headersSpec);
        when(headersSpec.header(eq("Authorization"), anyString())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(ResourceContentApiResponse.class))
                .thenThrow(HttpClientErrorException.create(HttpStatusCode.valueOf(404), "Not Found", null, null, null));

        // When
        List<ResourceContent> result = resourceContentServiceImpl.fetchContents(List.of(id));

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void fetchContents_shouldReturnEmpty_whenUnexpectedExceptionThrown() {
        // Given
        UUID id = UUID.randomUUID();
        when(authenticationService.getToken()).thenReturn(Optional.of("token-value"));
        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString(), any(Object.class))).thenReturn(headersSpec);
        when(headersSpec.header(eq("Authorization"), anyString())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenThrow(new RuntimeException("connection failed"));

        // When
        List<ResourceContent> result = resourceContentServiceImpl.fetchContents(List.of(id));

        // Then
        assertThat(result).isEmpty();
    }
}
