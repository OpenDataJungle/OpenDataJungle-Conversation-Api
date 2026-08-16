package com.opendatajungle.conversation.api.business.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HttpDownloadExceptionTest {

    @Test
    void constructor_shouldBuildMessageWithStatusCodeAndUrl() {
        // Given
        int statusCode = 502;
        String url = "http://laulem.com/file.pdf";

        // When
        HttpDownloadException exception = new HttpDownloadException(statusCode, url);

        // Then
        assertThat(exception.getMessage()).isEqualTo("HTTP error 502 when downloading from " + url);
    }
}
