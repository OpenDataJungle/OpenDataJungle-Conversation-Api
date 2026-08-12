package com.opendatajungle.conversation.api.client.controller;

import com.opendatajungle.conversation.api.business.service.ConversationService;
import com.opendatajungle.conversation.api.client.dto.ChatRequest;
import com.opendatajungle.conversation.api.client.dto.ChatResponse;
import com.opendatajungle.conversation.api.client.dto.ConversationMessageResponse;
import com.opendatajungle.conversation.api.client.dto.ConversationRequest;
import com.opendatajungle.conversation.api.client.dto.ConversationResponse;
import com.opendatajungle.conversation.api.client.dto.DeleteConversationsRequest;
import com.opendatajungle.conversation.api.client.dto.UpdateConversationRequest;
import com.opendatajungle.conversation.api.infra.conf.security.SecurityExpressions;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/conversations")
@Validated
@Slf4j
public class ConversationController {
    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PreAuthorize(SecurityExpressions.CONVERSATIONS_WRITE)
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ConversationResponse create(@RequestBody @Valid ConversationRequest request) {
        log.info("Creating conversation with title: {}", request.title());
        return ConversationResponse.map(conversationService.create(request.title(), request.systemMessage()));
    }

    @PreAuthorize(SecurityExpressions.CONVERSATIONS_READ)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ConversationResponse> listUserConversations() {
        log.info("Listing conversations for current user");
        return conversationService.findAllByUser().stream()
                .map(ConversationResponse::map)
                .toList();
    }

    @PreAuthorize(SecurityExpressions.CONVERSATIONS_READ)
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ConversationResponse getConversation(@PathVariable UUID id) {
        log.info("Fetching conversation id: {}", id);
        return ConversationResponse.map(conversationService.findById(id));
    }

    @PreAuthorize(SecurityExpressions.CONVERSATIONS_WRITE)
    @PatchMapping(value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ConversationResponse updateConversation(@PathVariable UUID id,
                                                   @RequestBody @Valid UpdateConversationRequest request) {
        log.info("Updating conversation id: {}", id);
        return ConversationResponse.map(conversationService.update(id, request.title(), request.systemMessage()));
    }

    @PreAuthorize(SecurityExpressions.CONVERSATIONS_WRITE)
    @PostMapping(value = "/{id}/chat",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ChatResponse chat(@PathVariable UUID id, @RequestBody @Valid ChatRequest request) {
        log.info("Chat message received for conversation id: {}", id);
        return new ChatResponse(conversationService.chat(request.toBusinessRequest(id)));
    }

    @PreAuthorize(SecurityExpressions.CONVERSATIONS_READ)
    @GetMapping(value = "/{id}/messages", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ConversationMessageResponse> getMessages(@PathVariable UUID id) {
        log.info("Fetching messages for conversation id: {}", id);
        return conversationService.getMessages(id).stream()
                .map(ConversationMessageResponse::new)
                .toList();
    }

    @PreAuthorize(SecurityExpressions.CONVERSATIONS_DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void deleteConversations(@RequestBody @Valid DeleteConversationsRequest request) {
        log.info("Deleting {} conversation(s)", request.ids().size());
        conversationService.deleteByIds(request.ids());
    }

    @PreAuthorize(SecurityExpressions.CONVERSATIONS_ADMIN)
    @GetMapping(value = "/admin", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ConversationResponse> listAllConversations() {
        log.info("Admin: listing all conversations");
        return conversationService.findAll().stream()
                .map(ConversationResponse::map)
                .toList();
    }
}
