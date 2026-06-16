package com.laulem.vectopath.conversation.api.shared.util;

public class TokenUtils {
    public static final long TOKEN_LENGTH = 4L;

    private TokenUtils() {
    }

    public static Long calculateToken(String text) {
        if (text == null || text.isBlank()) return 0L;
        return text.length() / TOKEN_LENGTH;
    }
}
