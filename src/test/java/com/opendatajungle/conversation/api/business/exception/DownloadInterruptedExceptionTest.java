package com.opendatajungle.conversation.api.business.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DownloadInterruptedExceptionTest {

    @Test
    void constructor_shouldBuildMessageWithUrlAndPreserveCause() {
        // Given
        String url = "http://laulem.com/file.pdf";
        Throwable cause = new RuntimeException("interrupted");

        // When
        DownloadInterruptedException exception = new DownloadInterruptedException(url, cause);

        // Then
        assertThat(exception.getMessage()).isEqualTo("Download interrupted for " + url);
        assertThat(exception.getCause()).isEqualTo(cause);
    }
}
