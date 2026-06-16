package com.laulem.vectopath.conversation.api.infra.conf.security;

public final class SecurityExpressions {
    public static final String CONVERSATIONS_READ = "hasAuthority(@securityScopesProperties.conversations.read)";
    public static final String CONVERSATIONS_WRITE = "hasAuthority(@securityScopesProperties.conversations.write)";
    public static final String CONVERSATIONS_DELETE = "hasAuthority(@securityScopesProperties.conversations.delete)";
    public static final String CONVERSATIONS_ADMIN = "hasAuthority(@securityScopesProperties.conversations.admin)";

    private SecurityExpressions() {
    }
}
