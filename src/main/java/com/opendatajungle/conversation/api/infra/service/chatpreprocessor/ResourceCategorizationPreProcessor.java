package com.opendatajungle.conversation.api.infra.service.chatpreprocessor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.opendatajungle.conversation.api.infra.model.ChatContext;
import com.opendatajungle.conversation.api.infra.model.ResourceRoutingStrategy;
import com.opendatajungle.conversation.api.infra.properties.ChatProperties;
import com.opendatajungle.conversation.api.infra.service.LlmModelKey;
import com.opendatajungle.conversation.api.infra.service.LlmModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

@Slf4j
public class ResourceCategorizationPreProcessor implements ChatPreProcessor {
    public static final int ORDER = 20;

    private final LlmModelService llmModelService;
    private final ObjectMapper objectMapper;
    private final ChatProperties chatProperties;

    public ResourceCategorizationPreProcessor(LlmModelService llmModelService,
                                              ObjectMapper objectMapper,
                                              ChatProperties chatProperties) {
        this.llmModelService = llmModelService;
        this.objectMapper = objectMapper;
        this.chatProperties = chatProperties;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    public ChatContext process(ChatContext chatContext) {
        List<UUID> resourceIds = chatContext.command().resourceIds();
        if (CollectionUtils.isEmpty(resourceIds) || !ResourceRoutingStrategy.NONE.equals(chatContext.resourceRoutingStrategy())) {
            return chatContext;
        }

        if (!llmModelService.hasCategorizerModel()) {
            log.debug("No categorizer model available, defaulting to INTERNAL_SEARCH strategy");
            return chatContext.withRoutingStrategy(ResourceRoutingStrategy.INTERNAL_SEARCH);
        }

        String strategy = getStrategy(chatContext.userMessage());

        log.debug("Resource categorization resolved strategy: {}", strategy);
        return chatContext.withRoutingStrategy(strategy);
    }

    public String getStrategy(String userMessage) {
        try {
            String rawDecision = llmModelService.getModel(LlmModelKey.CATEGORIZER)
                    .prompt()
                    .system(chatProperties.preProcessors().resourceCategorization().categorizerSystemPrompt())
                    .user(userMessage)
                    .call()
                    .content();

            log.debug("Categorizer raw response: {}", rawDecision);

            RoutingDecision decision = objectMapper.readValue(rawDecision, RoutingDecision.class);
            log.debug("Categorizer decision: {}", decision.strategy());
            return decision.strategy();
        } catch (Exception e) {
            log.warn("Categorizer model failed, falling back to INTERNAL_SEARCH strategy. Reason: {}", e.getMessage());
            return ResourceRoutingStrategy.INTERNAL_SEARCH;
        }
    }

    record RoutingDecision(@JsonProperty("strategy") String strategy) {
        @JsonCreator
        public RoutingDecision {
            if (strategy == null) strategy = ResourceRoutingStrategy.INTERNAL_SEARCH;
        }
    }
}

