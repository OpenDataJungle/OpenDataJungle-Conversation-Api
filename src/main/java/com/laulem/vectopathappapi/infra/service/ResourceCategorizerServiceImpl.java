package com.laulem.vectopathappapi.infra.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laulem.vectopathappapi.infra.model.ResourceRoutingDecision;
import com.laulem.vectopathappapi.infra.properties.ChatProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ResourceCategorizerServiceImpl implements ResourceCategorizerService {
    private final LlmModelService llmModelService;
    private final ChatProperties chatProperties;
    private final ObjectMapper objectMapper;

    public ResourceCategorizerServiceImpl(LlmModelService llmModelService,
                                          ChatProperties chatProperties,
                                          ObjectMapper objectMapper) {
        this.llmModelService = llmModelService;
        this.chatProperties = chatProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public ResourceRoutingDecision categorize(String userMessage, List<UUID> resourceIds) {
        try {
            String rawResponse = llmModelService.getModel(LlmModelKey.CATEGORIZER)
                    .prompt()
                    .system(chatProperties.categorizerSystemPrompt())
                    .user(userMessage)
                    .call()
                    .content();

            log.debug("Categorizer raw response: {}", rawResponse);

            ResourceRoutingDecision decision = objectMapper.readValue(rawResponse, ResourceRoutingDecision.class);
            log.debug("Categorizer decision: {}", decision.strategy());
            return decision;

        } catch (Exception e) {
            log.warn("Categorizer model failed, falling back to SEARCH strategy. Reason: {}", e.getMessage());
            return ResourceRoutingDecision.search();
        }
    }
}
