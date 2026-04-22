package com.laulem.vectopathappapi.business.service;

import com.laulem.vectopathappapi.business.exception.NotFoundException;
import com.laulem.vectopathappapi.business.exception.ParamException;
import com.laulem.vectopathappapi.business.model.SendChatMessageCommand;
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
        if (!Strings.isBlank(title)) {
            conversation.setTitle(title);
        }
        if (!Strings.isBlank(systemMessage)) {
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
        if (Strings.isBlank(sendChatMessageCommand.message()) || sendChatMessageCommand.conversationId() == null) {
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








