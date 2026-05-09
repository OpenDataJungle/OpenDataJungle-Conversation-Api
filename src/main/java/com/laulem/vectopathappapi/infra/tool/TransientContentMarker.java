package com.laulem.vectopathappapi.infra.tool;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

/**
 * Utility class implementing the TransientContent pattern.
 * <p>
 * Content wrapped with these markers is injected in the user prompt for LLM inference,
 * but should be stripped before any message is persisted to the chat memory.
 * </p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TransientContentMarker {
    public static final String START = "[[TRANSIENT_START]]";
    public static final String END = "[[TRANSIENT_END]]";

    private static final Pattern STRIP_PATTERN = Pattern.compile(
            Pattern.quote(START) + ".*?" + Pattern.quote(END) + "\\n?",
            Pattern.DOTALL
    );

    /**
     * Wraps the given content with transient markers so it can be identified and stripped later.
     *
     * @param content the content to wrap (e.g. file contents)
     * @return the wrapped content, terminated by a newline
     */
    public static String wrap(String content) {
        return START + "\n" + content + "\n" + END + "\n";
    }

    /**
     * Strips all transient blocks from the given text.
     *
     * @param text the raw text that may contain transient blocks
     * @return the text with all transient blocks removed, trimmed
     */
    public static String strip(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        return STRIP_PATTERN.matcher(text).replaceAll("").strip();
    }

    /**
     * Returns {@code true} if the given text contains at least one transient block.
     *
     * @param text the text to inspect
     * @return {@code true} if a transient block is present
     */
    public static boolean containsTransientContent(String text) {
        return text != null && text.contains(START);
    }
}

