package com.opendatajungle.conversation.api.business.exception;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceDeletionExceptionTest {

    @Test
    void constructor_shouldBuildMessageWithResourceIdAndPreserveCause() {
        // Given
        UUID resourceId = UUID.randomUUID();
        Throwable cause = new RuntimeException("db error");

        // When
        ResourceDeletionException exception = new ResourceDeletionException(resourceId, cause);

        // Then
        assertThat(exception.getMessage()).isEqualTo("Failed to delete resource: " + resourceId);
        assertThat(exception.getCause()).isEqualTo(cause);
    }
}
