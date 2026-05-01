package com.laulem.vectopathappapi.infra.service;

import com.laulem.vectopathappapi.infra.model.ResourceContent;

import java.util.List;
import java.util.UUID;

public interface ResourceContentService {
    List<ResourceContent> fetchContents(List<UUID> resourceIds);
}
