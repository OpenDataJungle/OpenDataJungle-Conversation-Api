package com.laulem.vectopath.conversation.api.infra.service;

import lombok.Getter;

@Getter
public enum LlmModelKey {
    DEFAULT("default"),
    SPEED("speed"),
    CATEGORIZER("categorizer"),
    LONG_CONTEXT("long-context");

    private final String key;

    LlmModelKey(String key) {
        this.key = key;
    }
}
