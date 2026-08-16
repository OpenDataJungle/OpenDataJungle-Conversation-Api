package com.opendatajungle.conversation.api.infra.dto;

import com.opendatajungle.conversation.api.infra.model.ResourceContent;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceContentApiResponseTest {

    @Test
    void toResourceContent_shouldMapAllFields() {
        // Given
        UUID id = UUID.randomUUID();
        ResourceContentApiResponse response = new ResourceContentApiResponse(id, "file.txt", "file content");

        // When
        ResourceContent resourceContent = response.toResourceContent();

        // Then
        assertThat(resourceContent.id()).isEqualTo(id);
        assertThat(resourceContent.name()).isEqualTo("file.txt");
        assertThat(resourceContent.content()).isEqualTo("file content");
    }
}
