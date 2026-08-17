package com.opendatajungle.conversation.api.infra.service;

import com.opendatajungle.commons.business.service.AuthenticationUseCase;
import com.opendatajungle.conversation.api.infra.dto.ResourceContentApiResponse;
import com.opendatajungle.conversation.api.infra.model.ResourceContent;
import com.opendatajungle.conversation.api.infra.properties.OpenDataJungleKnowledgeApiProperties;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class ResourceContentServiceImpl implements ResourceContentService {
    private static final int MAX_CONCURRENT_FETCHES = 8;
    private final RestClient restClient;
    private final AuthenticationUseCase authenticationService;
    private final String resourceContentPath;
    private final ExecutorService executor;

    public ResourceContentServiceImpl(RestClient.Builder restClientBuilder,
                                      OpenDataJungleKnowledgeApiProperties properties,
                                      AuthenticationUseCase authenticationService) {
        this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
        this.authenticationService = authenticationService;
        this.resourceContentPath = properties.resourceContentPath();
        this.executor = Executors.newFixedThreadPool(MAX_CONCURRENT_FETCHES,
                Thread.ofPlatform().name("resource-content-", 0).daemon().factory());
    }

    @PreDestroy
    public void destroy() {
        executor.shutdown();
    }

    @Override
    public List<ResourceContent> fetchContents(List<UUID> resourceIds) {
        if (CollectionUtils.isEmpty(resourceIds)) {
            return Collections.emptyList();
        }

        String bearerToken = authenticationService.getToken().orElse("");

        return resourceIds.stream()
                .map(id -> CompletableFuture.supplyAsync(() -> fetchContent(id, bearerToken), executor))
                .toList()
                .stream()
                .map(CompletableFuture::join)
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
            log.error("Failed to fetch content for resource {}: {} - {}", id, e.getStatusCode(), e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Unexpected error fetching content for resource {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }
}
