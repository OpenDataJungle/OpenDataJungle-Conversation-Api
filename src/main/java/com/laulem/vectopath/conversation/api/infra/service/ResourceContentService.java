package com.laulem.vectopath.conversation.api.infra.service;

import com.laulem.vectopath.conversation.api.infra.model.ResourceContent;

import java.util.List;
import java.util.UUID;

public interface ResourceContentService {
    List<ResourceContent> fetchContents(List<UUID> resourceIds);
}
