package com.opendatajungle.conversation.api.infra.dto;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SearchRequestTest {

    @Test
    void constructor_shouldKeepProvidedValues_whenValid() {
        // Given
        List<UUID> resourceIds = List.of(UUID.randomUUID());

        // When
        SearchRequest request = new SearchRequest("query", 25, 0.75, resourceIds);

        // Then
        assertThat(request.query()).isEqualTo("query");
        assertThat(request.limit()).isEqualTo(25);
        assertThat(request.minSimilarity()).isEqualTo(0.75);
        assertThat(request.resourceIds()).isEqualTo(resourceIds);
    }

    @Test
    void constructor_shouldDefaultLimitToTen_whenLimitIsNull() {
        // Given & When
        SearchRequest request = new SearchRequest("query", null, 0.5, List.of());

        // Then
        assertThat(request.limit()).isEqualTo(10);
    }

    @Test
    void constructor_shouldDefaultLimitToTen_whenLimitIsNotPositive() {
        // Given & When
        SearchRequest request = new SearchRequest("query", 0, 0.5, List.of());

        // Then
        assertThat(request.limit()).isEqualTo(10);
    }

    @Test
    void constructor_shouldDefaultMinSimilarityToZero_whenMinSimilarityIsNull() {
        // Given & When
        SearchRequest request = new SearchRequest("query", 5, null, List.of());

        // Then
        assertThat(request.minSimilarity()).isEqualTo(0.0);
    }

    @Test
    void constructor_shouldDefaultMinSimilarityToZero_whenMinSimilarityIsNegative() {
        // Given & When
        SearchRequest request = new SearchRequest("query", 5, -0.1, List.of());

        // Then
        assertThat(request.minSimilarity()).isEqualTo(0.0);
    }

    @Test
    void constructor_shouldDefaultMinSimilarityToZero_whenMinSimilarityIsAboveOne() {
        // Given & When
        SearchRequest request = new SearchRequest("query", 5, 1.1, List.of());

        // Then
        assertThat(request.minSimilarity()).isEqualTo(0.0);
    }
}
