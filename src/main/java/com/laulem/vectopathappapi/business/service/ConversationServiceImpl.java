package com.laulem.vectopathappapi.business.service;

import com.laulem.vectopathappapi.business.exception.NotFoundException;
import com.laulem.vectopathappapi.business.exception.ParamException;
import com.laulem.vectopathappapi.business.model.ChatResult;
import com.laulem.vectopathappapi.business.model.Conversation;
import com.laulem.vectopathappapi.business.model.ConversationMessage;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ConversationServiceImpl implements ConversationService {
    private static final String UNAUTHENTICATED_MSG = "Authenticated user not found";

    private final ConversationRepository conversationRepository;
    private final ConversationMessageRepository conversationMessageRepository;
    private final AuthenticationService authenticationService;
    private final ChatService chatService;

    public ConversationServiceImpl(ConversationRepository conversationRepository,
                                   ConversationMessageRepository conversationMessageRepository,
                                   AuthenticationService authenticationService,
                                   ChatService chatService) {
        this.conversationRepository = conversationRepository;
        this.conversationMessageRepository = conversationMessageRepository;
        this.authenticationService = authenticationService;
        this.chatService = chatService;
    }

    @Override
    public Conversation create(String title, String systemMessage) {
        String userId = authenticationService.getUser()
                .orElseThrow(() -> new IllegalStateException(UNAUTHENTICATED_MSG));

        Conversation conversation = new Conversation();
        conversation.setId(UUID.randomUUID());
        conversation.setUserId(userId);
        conversation.setTitle(title);
        conversation.setSystemMessage(systemMessage);
        conversation.setCreatedAt(LocalDateTime.now());

        return conversationRepository.save(conversation);
    }

    @Override
    public Conversation findById(UUID conversationId) {
        String userId = authenticationService.getUser()
                .orElseThrow(() -> new IllegalStateException(UNAUTHENTICATED_MSG));

        return conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new NotFoundException("Conversation", conversationId.toString()));
    }

    @Override
    public List<Conversation> findAllByUser() {
        String userId = authenticationService.getUser()
                .orElseThrow(() -> new IllegalStateException(UNAUTHENTICATED_MSG));

        return conversationRepository.findAllByUserId(userId);
    }

    @Override
    public List<Conversation> findAll() {
        return conversationRepository.findAll();
    }

    @Override
    public Conversation update(UUID conversationId, String title, String systemMessage) {
        Conversation conversation = findById(conversationId);
        if (!Strings.isBlank(title)) {
            conversation.setTitle(title);
        }
        if (!Strings.isBlank(systemMessage)) {
            conversation.setSystemMessage(systemMessage);
        }
        return conversationRepository.save(conversation);
    }

    @Override
    public void deleteByIds(List<UUID> conversationIdsToDelete) {
        if (CollectionUtils.isEmpty(conversationIdsToDelete)) {
            throw new ParamException("REQUIRED", "At least one conversation ID must be provided", "ids");
        }

        String userId = authenticationService.getUser()
                .orElseThrow(() -> new IllegalStateException(UNAUTHENTICATED_MSG));

        // Check if all ids are in the user scope before deleting any to avoid partial deletes
        conversationIdsToDelete.forEach(idToDelete -> conversationRepository.findByIdAndUserId(idToDelete, userId)
                .orElseThrow(() -> new NotFoundException("Conversation", idToDelete.toString())));

        conversationRepository.delete(conversationIdsToDelete);
    }

    @Override
    public ChatResult chat(UUID conversationId, String message) {
        if (Strings.isBlank(message) || conversationId == null) {
            throw new ParamException("REQUIRED", "Message and conversation ID must be provided", "message or conversation id");
        }
        Conversation conversation = findById(conversationId);
        return chatService.chat(conversationId, conversation.getSystemMessage(), message);
    }

    @Override
    public List<ConversationMessage> getMessages(UUID conversationId) {
        findById(conversationId);
        return conversationMessageRepository.findAllByConversationId(conversationId);
    }
}








