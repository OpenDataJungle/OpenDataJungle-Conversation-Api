package com.opendatajungle.conversation.api.infra.service;

import com.opendatajungle.commons.business.service.AuthenticationUseCase;
import com.opendatajungle.conversation.api.infra.model.ResourceContent;
import com.opendatajungle.conversation.api.infra.dto.ResourceContentApiResponse;
import com.opendatajungle.conversation.api.infra.properties.OpenDataJungleKnowledgeApiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public class ResourceContentServiceImpl implements ResourceContentService {

    private final RestClient restClient;
    private final AuthenticationUseCase authenticationService;
    private final String resourceContentPath;

    public ResourceContentServiceImpl(RestClient.Builder restClientBuilder,
                                      OpenDataJungleKnowledgeApiProperties properties,
                                      AuthenticationUseCase authenticationService) {
        this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
        this.authenticationService = authenticationService;
        this.resourceContentPath = properties.resourceContentPath();
    }

    @Override
    public List<ResourceContent> fetchContents(List<UUID> resourceIds) {
        if (CollectionUtils.isEmpty(resourceIds)) {
            return Collections.emptyList();
        }

        String bearerToken = authenticationService.getToken().orElse("");

        return resourceIds.parallelStream()
                .map(id -> fetchContent(id, bearerToken))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private Optional<ResourceContent> fetchContent(UUID id, String bearerToken) {
        try {
            ResourceContentApiResponse response = restClient.get()
                    .uri(resourceContentPath + "/{id}/content", id)
                    .header("Authorization", "Bearer " + bearerToken)
                    .retrieve()
                    .body(ResourceContentApiResponse.class);
            return Optional.ofNullable(response).map(ResourceContentApiResponse::toResourceContent);
        } catch (HttpClientErrorException e) {
            log.warn("Failed to fetch content for resource {}: {} - {}", id, e.getStatusCode(), e.getResponseBodyAsString());
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Unexpected error fetching content for resource {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }
}
