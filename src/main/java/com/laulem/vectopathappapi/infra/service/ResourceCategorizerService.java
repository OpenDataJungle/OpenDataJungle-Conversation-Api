package com.laulem.vectopathappapi.infra.service;

import com.laulem.vectopathappapi.infra.model.ResourceRoutingDecision;

import java.util.List;
import java.util.UUID;

public interface ResourceCategorizerService {
    ResourceRoutingDecision categorize(String userMessage, List<UUID> resourceIds);
}
