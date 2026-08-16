package com.opendatajungle.conversation.api.client.controller;

import com.opendatajungle.conversation.api.business.model.ChatResult;
import com.opendatajungle.conversation.api.business.model.Conversation;
import com.opendatajungle.conversation.api.business.model.ConversationMessage;
import com.opendatajungle.conversation.api.business.model.SendChatMessageCommand;
import com.opendatajungle.conversation.api.business.model.ToolResult;
import com.opendatajungle.conversation.api.business.service.ConversationService;
import com.opendatajungle.conversation.api.client.dto.ChatRequest;
import com.opendatajungle.conversation.api.client.dto.ChatResponse;
import com.opendatajungle.conversation.api.client.dto.ConversationMessageResponse;
import com.opendatajungle.conversation.api.client.dto.ConversationRequest;
import com.opendatajungle.conversation.api.client.dto.ConversationResponse;
import com.opendatajungle.conversation.api.client.dto.DeleteConversationsRequest;
import com.opendatajungle.conversation.api.client.dto.UpdateConversationRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationControllerTest {

    @Mock
    private ConversationService conversationService;

    @InjectMocks
    private ConversationController controller;

    @Test
    void create_shouldReturnMappedConversation_whenRequestIsValid() {
        // Given
        ConversationRequest request = new ConversationRequest("Trip planning", "You are a travel assistant");
        Conversation conversation = new Conversation(UUID.randomUUID(), "user-1", "Trip planning",
                "You are a travel assistant", LocalDateTime.now(), null);
        when(conversationService.create("Trip planning", "You are a travel assistant")).thenReturn(conversation);

        // When
        ConversationResponse response = controller.create(request);

        // Then
        assertThat(response).isEqualTo(new ConversationResponse(conversation));
    }

    @Test
    void listUserConversations_shouldReturnMappedConversations_whenServiceReturnsResults() {
        // Given
        Conversation conversation = new Conversation(UUID.randomUUID(), "user-1", "Trip planning",
                null, LocalDateTime.now(), null);
        when(conversationService.findAllByUser()).thenReturn(List.of(conversation));

        // When
        List<ConversationResponse> responses = controller.listUserConversations();

        // Then
        assertThat(responses).containsExactly(new ConversationResponse(conversation));
    }

    @Test
    void listUserConversations_shouldReturnEmptyList_whenServiceReturnsNoResults() {
        // Given
        when(conversationService.findAllByUser()).thenReturn(List.of());

        // When
        List<ConversationResponse> responses = controller.listUserConversations();

        // Then
        assertThat(responses).isEmpty();
    }

    @Test
    void getConversation_shouldReturnMappedConversation_whenFound() {
        // Given
        UUID id = UUID.randomUUID();
        Conversation conversation = new Conversation(id, "user-1", "Trip planning", null, LocalDateTime.now(), null);
        when(conversationService.findById(id)).thenReturn(conversation);

        // When
        ConversationResponse response = controller.getConversation(id);

        // Then
        assertThat(response).isEqualTo(new ConversationResponse(conversation));
    }

    @Test
    void updateConversation_shouldReturnMappedConversation_whenRequestIsValid() {
        // Given
        UUID id = UUID.randomUUID();
        UpdateConversationRequest request = new UpdateConversationRequest("New title", "New system message");
        Conversation conversation = new Conversation(id, "user-1", "New title", "New system message", LocalDateTime.now(), LocalDateTime.now());
        when(conversationService.update(id, "New title", "New system message")).thenReturn(conversation);

        // When
        ConversationResponse response = controller.updateConversation(id, request);

        // Then
        assertThat(response).isEqualTo(new ConversationResponse(conversation));
    }

    @Test
    void chat_shouldReturnChatResponse_whenRequestIsValid() {
        // Given
        UUID id = UUID.randomUUID();
        ChatRequest request = new ChatRequest("Hello there", List.of(), Set.of("search"), "gpt-4");
        ChatResult result = new ChatResult("Hi, how can I help?", List.of(new ToolResult("t1", "query", Map.of("k", "v"))));
        when(conversationService.chat(request.toBusinessRequest(id))).thenReturn(result);

        // When
        ChatResponse response = controller.chat(id, request);

        // Then
        assertThat(response).isEqualTo(new ChatResponse(result));
        verify(conversationService).chat(request.toBusinessRequest(id));
    }

    @Test
    void getMessages_shouldReturnMappedMessages_whenServiceReturnsResults() {
        // Given
        UUID id = UUID.randomUUID();
        ConversationMessage message = new ConversationMessage(UUID.randomUUID(), id, "USER", "Hello", LocalDateTime.now(), List.of());
        when(conversationService.getMessages(id)).thenReturn(List.of(message));

        // When
        List<ConversationMessageResponse> responses = controller.getMessages(id);

        // Then
        assertThat(responses).containsExactly(new ConversationMessageResponse(message));
    }

    @Test
    void deleteConversations_shouldDelegateToService_whenRequestIsValid() {
        // Given
        List<UUID> ids = List.of(UUID.randomUUID(), UUID.randomUUID());
        DeleteConversationsRequest request = new DeleteConversationsRequest(ids);

        // When
        controller.deleteConversations(request);

        // Then
        verify(conversationService).deleteByIds(ids);
    }

    @Test
    void listAllConversations_shouldReturnMappedConversations_whenServiceReturnsResults() {
        // Given
        Conversation conversation = new Conversation(UUID.randomUUID(), "user-2", "Admin view", null, LocalDateTime.now(), null);
        when(conversationService.findAll()).thenReturn(List.of(conversation));

        // When
        List<ConversationResponse> responses = controller.listAllConversations();

        // Then
        assertThat(responses).containsExactly(new ConversationResponse(conversation));
    }
}
