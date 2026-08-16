package com.opendatajungle.conversation.api.business.service;

import com.opendatajungle.commons.business.exception.NotFoundException;
import com.opendatajungle.commons.business.exception.ParamException;
import com.opendatajungle.commons.business.service.AuthenticationUseCase;
import com.opendatajungle.commons.util.CollectionUtils;
import com.opendatajungle.commons.util.StringUtils;
import com.opendatajungle.conversation.api.business.model.ChatResult;
import com.opendatajungle.conversation.api.business.model.Conversation;
import com.opendatajungle.conversation.api.business.model.ConversationMessage;
import com.opendatajungle.conversation.api.business.model.SendChatMessageCommand;
import com.opendatajungle.conversation.api.business.repository.ConversationMessageRepository;
import com.opendatajungle.conversation.api.business.repository.ConversationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ConversationServiceImpl implements ConversationService {
    private final ConversationRepository conversationRepository;
    private final ConversationMessageRepository conversationMessageRepository;
    private final AuthenticationUseCase authenticationService;
    private final ChatService chatService;

    public ConversationServiceImpl(ConversationRepository conversationRepository,
                                   ConversationMessageRepository conversationMessageRepository,
                                   AuthenticationUseCase authenticationService,
                                   ChatService chatService) {
        this.conversationRepository = conversationRepository;
        this.conversationMessageRepository = conversationMessageRepository;
        this.authenticationService = authenticationService;
        this.chatService = chatService;
    }

    @Override
    public Conversation create(String title, String systemMessage) {
        String userId = authenticationService.getCurrentUser();

        Conversation conversation = new Conversation();
        conversation.setId(UUID.randomUUID());
        conversation.setUserId(userId);
        conversation.setTitle(title);
        conversation.setSystemMessage(systemMessage);
        LocalDateTime now = LocalDateTime.now();
        conversation.setCreatedAt(now);
        conversation.setLastMessageAt(now);

        return conversationRepository.save(conversation);
    }

    @Override
    public Conversation findById(UUID conversationId) {
        String userId = authenticationService.getCurrentUser();

        return conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new NotFoundException("Conversation", conversationId.toString()));
    }

    @Override
    public List<Conversation> findAllByUser() {
        String userId = authenticationService.getCurrentUser();

        return conversationRepository.findAllByUserId(userId);
    }

    @Override
    public List<Conversation> findAll() {
        return conversationRepository.findAll();
    }

    @Override
    public Conversation update(UUID conversationId, String title, String systemMessage) {
        Conversation conversation = findById(conversationId);
        if (StringUtils.hasText(title)) {
            conversation.setTitle(title);
        }
        if (StringUtils.hasText(systemMessage)) {
            conversation.setSystemMessage(systemMessage);
        }
        conversation.setLastMessageAt(LocalDateTime.now());
        return conversationRepository.save(conversation);
    }

    @Override
    public void deleteByIds(List<UUID> conversationIdsToDelete) {
        if (CollectionUtils.isEmpty(conversationIdsToDelete)) {
            throw new ParamException("REQUIRED", "At least one conversation ID must be provided", "ids");
        }

        String userId = authenticationService.getCurrentUser();

        // Check if all ids are in the user scope before deleting any to avoid partial deletes
        conversationIdsToDelete.forEach(idToDelete -> conversationRepository.findByIdAndUserId(idToDelete, userId)
                .orElseThrow(() -> new NotFoundException("Conversation", idToDelete.toString())));

        conversationRepository.delete(conversationIdsToDelete);
    }

    @Override
    public ChatResult chat(SendChatMessageCommand sendChatMessageCommand) {
        if (StringUtils.isNullOrBlank(sendChatMessageCommand.message()) || sendChatMessageCommand.conversationId() == null) {
            throw new ParamException("REQUIRED", "Message and conversation ID must be provided", "message or conversation id");
        }
        Conversation conversation = findById(sendChatMessageCommand.conversationId());
        ChatResult chatResult = chatService.chat(sendChatMessageCommand, conversation.getSystemMessage());

        conversationRepository.updateLastMessageAtToNow(conversation);

        return chatResult;
    }

    @Override
    public List<ConversationMessage> getMessages(UUID conversationId) {
        findById(conversationId);
        return conversationMessageRepository.findAllByConversationId(conversationId);
    }
}


