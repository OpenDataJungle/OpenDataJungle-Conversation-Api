package com.laulem.vectopathappapi.infra.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laulem.vectopathappapi.business.model.ConversationMessage;
import com.laulem.vectopathappapi.business.model.ToolResult;
import com.laulem.vectopathappapi.business.service.ConversationMessageRepository;
import com.laulem.vectopathappapi.infra.entity.ConversationMessageEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
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
