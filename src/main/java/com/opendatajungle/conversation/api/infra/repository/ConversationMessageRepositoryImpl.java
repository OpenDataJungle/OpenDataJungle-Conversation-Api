package com.opendatajungle.conversation.api.infra.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opendatajungle.conversation.api.business.model.ConversationMessage;
import com.opendatajungle.conversation.api.business.model.ToolResult;
import com.opendatajungle.conversation.api.business.repository.ConversationMessageRepository;
import com.opendatajungle.conversation.api.infra.entity.ConversationMessageEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class ConversationMessageRepositoryImpl implements ConversationMessageRepository {
    private final ConversationMessageJpaRepository conversationMessageRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<ConversationMessage> findAllByConversationId(UUID conversationId) {
        return conversationMessageRepository.findAllByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream()
                .map(this::toConversationMessage)
                .toList();
    }

    private ConversationMessage toConversationMessage(ConversationMessageEntity entity) {
        List<ToolResult> toolResults = null;
        if (entity.getToolResults() != null) {
            try {
                toolResults = objectMapper.readValue(entity.getToolResults(), new TypeReference<>() {});
            } catch (Exception e) {
                log.error("Failed to deserialize tool_results for message {}", entity.getId(), e);
            }
        }
        return new ConversationMessage(
                entity.getId(),
                entity.getConversationId(),
                entity.getType(),
                entity.getContent(),
                entity.getCreatedAt(),
                toolResults
        );
    }
}
