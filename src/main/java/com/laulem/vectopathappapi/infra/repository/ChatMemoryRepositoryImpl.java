package com.laulem.vectopathappapi.infra.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laulem.vectopathappapi.business.model.ToolResult;
import com.laulem.vectopathappapi.infra.entity.ConversationMessageEntity;
import com.laulem.vectopathappapi.infra.properties.ChatProperties;
import com.laulem.vectopathappapi.infra.tool.ChatRequestHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Repository
public class ChatMemoryRepositoryImpl implements ChatMemoryRepository {
    public static final String ASSISTANT = "ASSISTANT";
    public static final String ALREADY_SAVED = "alreadySaved";
    private final ChatProperties chatProperties;
    private final ConversationMessageJpaRepository conversationMessageRepository;
    private final ChatRequestHolder chatRequestHolder;
    private final ObjectMapper objectMapper;

    @Override
    public List<String> findConversationIds() {
        return conversationMessageRepository.findDistinctConversationIds()
                .stream()
                .map(UUID::toString)
                .toList();
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        return conversationMessageRepository
                .findAllByConversationIdAndInContextTrueOrderByCreatedAtAsc(UUID.fromString(conversationId))
                .stream()
                .map(this::toMessage)
                .map(message -> {
                    message.getMetadata().put(ALREADY_SAVED, true);
                    return message;
                })
                .toList();
    }

    /**
     * Persists only the NEW messages (appended since the last save) and
     * re-applies the token-based context window. Messages are NEVER deleted.
     */
    @Override
    @Transactional
    public void saveAll(String conversationId, List<Message> messages) {
        UUID convId = UUID.fromString(conversationId);
        List<Message> newMessages = messages.stream().filter(message -> !message.getMetadata().containsKey(ALREADY_SAVED)).toList();

        if (!newMessages.isEmpty()) {
            List<ConversationMessageEntity> entities = newMessages.stream()
                    .map(message -> toEntity(convId, message))
                    .toList();
            conversationMessageRepository.saveAll(entities);
        }

        applyTokenWindowContext(convId);
    }

    @Override
    @Transactional
    public void deleteByConversationId(String conversationId) {
        conversationMessageRepository.setAllOutOfContext(UUID.fromString(conversationId));
    }

    private void applyTokenWindowContext(UUID conversationId) {
        List<ConversationMessageEntity> conversationMessages = conversationMessageRepository.findAllByConversationIdAndInContextTrueOrderByCreatedAtDesc(conversationId);

        List<UUID> inContextIds = new ArrayList<>();
        int totalTokens = 0;

        for (ConversationMessageEntity message : conversationMessages) {
            int tokens = estimateTokens(message.getContent());
            if (totalTokens + tokens <= chatProperties.getMaxContextTokens()) {
                inContextIds.add(message.getId());
                totalTokens += tokens;
            } else {
                break;
            }
        }

        log.debug("Token window for conversation {}: {} messages, ~{} tokens", conversationId, inContextIds.size(), totalTokens);

        if (!inContextIds.isEmpty()) {
            conversationMessageRepository.setAllOutOfContextButIds(conversationId, inContextIds);
        }
    }

    private int estimateTokens(String text) {
        if (text == null || text.isBlank()) return 1;
        return Math.max(1, text.length() / 4);
    }

    private Message toMessage(ConversationMessageEntity entity) {
        return switch (entity.getType()) {
            case "USER" -> new UserMessage(entity.getContent());
            case ASSISTANT -> new AssistantMessage(entity.getContent());
            case "SYSTEM" -> new SystemMessage(entity.getContent());
            default -> throw new IllegalStateException("Unknown message type: " + entity.getType());
        };
    }

    private ConversationMessageEntity toEntity(UUID conversationId, Message message) {
        ConversationMessageEntity entity = new ConversationMessageEntity();
        entity.setId(UUID.randomUUID());
        entity.setConversationId(conversationId);
        entity.setType(message.getMessageType().getValue().toUpperCase());
        entity.setContent(message.getText());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setInContext(true);

        // Tool results are already collected at this point (tool execution precedes saveAll).
        // We attach them directly to the ASSISTANT entity to avoid any separate UPDATE query.
        if (ASSISTANT.equals(entity.getType())) {
            List<ToolResult> toolResults = chatRequestHolder.getToolResults();
            if (!toolResults.isEmpty()) {
                try {
                    entity.setToolResults(objectMapper.writeValueAsString(toolResults));
                } catch (JsonProcessingException e) {
                    log.error("Failed to serialize tool results for assistant message {}", entity.getId(), e);
                }
            }
        }
        return entity;
    }
}
