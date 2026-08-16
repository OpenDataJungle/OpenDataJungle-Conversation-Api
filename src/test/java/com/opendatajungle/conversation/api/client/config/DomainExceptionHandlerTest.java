package com.opendatajungle.conversation.api.client.config;

import com.opendatajungle.commons.client.dto.GeneralResponseException;
import com.opendatajungle.conversation.api.business.exception.DownloadInterruptedException;
import com.opendatajungle.conversation.api.business.exception.HttpDownloadException;
import com.opendatajungle.conversation.api.business.exception.ResourceDeletionException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DomainExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private final DomainExceptionHandler handler = new DomainExceptionHandler();

    @Test
    void handleHttpDownloadException_shouldReturnInternalServerError() {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/conversations");
        HttpDownloadException ex = new HttpDownloadException(502, "https://laulem.com/file");

        // When
        ResponseEntity<GeneralResponseException> response = handler.handleHttpDownloadException(ex, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("HTTP_DOWNLOAD_ERROR");
        assertThat(response.getBody().message()).isEqualTo(ex.getMessage());
    }

    @Test
    void handleDownloadInterruptedException_shouldReturnInternalServerError() {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/conversations");
        DownloadInterruptedException ex = new DownloadInterruptedException("https://laulem.com/file", new RuntimeException("io error"));

        // When
        ResponseEntity<GeneralResponseException> response = handler.handleDownloadInterruptedException(ex, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("DOWNLOAD_INTERRUPTED");
        assertThat(response.getBody().message()).isEqualTo(ex.getMessage());
    }

    @Test
    void handleVectorStoreDeletionException_shouldReturnInternalServerError() {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/conversations");
        ResourceDeletionException ex = new ResourceDeletionException(UUID.randomUUID(), new RuntimeException("db error"));

        // When
        ResponseEntity<GeneralResponseException> response = handler.handleVectorStoreDeletionException(ex, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("VECTOR_STORE_DELETION_ERROR");
        assertThat(response.getBody().message()).isEqualTo(ex.getMessage());
    }
}
