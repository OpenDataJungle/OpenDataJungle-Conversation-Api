package com.opendatajungle.conversation.api.infra.tool;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TransientContentMarkerTest {

    @Test
    void wrap_shouldSurroundContentWithMarkers() {
        // Given
        String content = "file content";

        // When
        String wrapped = TransientContentMarker.wrap(content);

        // Then
        assertThat(wrapped).isEqualTo("[[TRANSIENT_START]]\nfile content\n[[TRANSIENT_END]]\n");
    }

    @Test
    void strip_shouldRemoveTransientBlock_whenPresent() {
        // Given
        String text = "before " + TransientContentMarker.wrap("secret content") + "after";

        // When
        String stripped = TransientContentMarker.strip(text);

        // Then
        assertThat(stripped).isEqualTo("before after");
    }

    @Test
    void strip_shouldRemoveMultipleTransientBlocks() {
        // Given
        String text = TransientContentMarker.wrap("first") + TransientContentMarker.wrap("second");

        // When
        String stripped = TransientContentMarker.strip(text);

        // Then
        assertThat(stripped).isEmpty();
    }

    @Test
    void strip_shouldReturnNull_whenTextIsNull() {
        assertThat(TransientContentMarker.strip(null)).isNull();
    }

    @Test
    void strip_shouldReturnBlankText_whenTextIsBlank() {
        assertThat(TransientContentMarker.strip("   ")).isEqualTo("   ");
    }

    @Test
    void strip_shouldReturnStrippedText_whenNoTransientBlockPresent() {
        assertThat(TransientContentMarker.strip("  plain text  ")).isEqualTo("plain text");
    }

    @Test
    void containsTransientContent_shouldReturnTrue_whenTextContainsStartMarker() {
        // Given
        String text = TransientContentMarker.wrap("content");

        // When & Then
        assertThat(TransientContentMarker.containsTransientContent(text)).isTrue();
    }

    @Test
    void containsTransientContent_shouldReturnFalse_whenTextDoesNotContainStartMarker() {
        assertThat(TransientContentMarker.containsTransientContent("plain text")).isFalse();
    }

    @Test
    void containsTransientContent_shouldReturnFalse_whenTextIsNull() {
        assertThat(TransientContentMarker.containsTransientContent(null)).isFalse();
    }
}
