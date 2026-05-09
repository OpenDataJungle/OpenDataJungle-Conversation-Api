package com.laulem.vectopathappapi.infra.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceRoutingStrategy {
    public static final String BASIC = "BASIC";
    public static final String INCLUDE_IN_PROMPT = "INCLUDE_IN_PROMPT";
}
