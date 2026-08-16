package com.opendatajungle.conversation.api.infra.dto;

import com.opendatajungle.conversation.api.infra.model.Search;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SearchResponseTest {

    @Test
    void toSearch_shouldMapAllFields() {
        // Given
        UUID vectorId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 2, 10, 0);
        SearchResponse response = new SearchResponse(
                vectorId, resourceId, "resource-name", "content", "text/plain",
                "metadata", 0.9, createdAt, updatedAt);

        // When
        Search search = SearchResponse.toSearch(response);

        // Then
        assertThat(search.vectorId()).isEqualTo(vectorId);
        assertThat(search.resourceId()).isEqualTo(resourceId);
        assertThat(search.resourceName()).isEqualTo("resource-name");
        assertThat(search.content()).isEqualTo("content");
        assertThat(search.contentType()).isEqualTo("text/plain");
        assertThat(search.metadata()).isEqualTo("metadata");
        assertThat(search.similarityScore()).isEqualTo(0.9);
        assertThat(search.createdAt()).isEqualTo(createdAt);
        assertThat(search.updatedAt()).isEqualTo(updatedAt);
    }
}
