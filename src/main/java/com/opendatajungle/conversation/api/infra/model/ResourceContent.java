package com.opendatajungle.conversation.api.infra.model;

import java.util.UUID;

public record ResourceContent(UUID id, String name, String content) {
}
