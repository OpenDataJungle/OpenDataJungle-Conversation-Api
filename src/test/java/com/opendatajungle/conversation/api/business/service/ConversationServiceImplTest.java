package com.opendatajungle.conversation.api.business.service;

import com.opendatajungle.commons.business.exception.NotFoundException;
import com.opendatajungle.commons.business.exception.ParamException;
import com.opendatajungle.commons.business.service.AuthenticationUseCase;
import com.opendatajungle.conversation.api.business.model.ChatResult;
import com.opendatajungle.conversation.api.business.model.Conversation;
import com.opendatajungle.conversation.api.business.model.ConversationMessage;
import com.opendatajungle.conversation.api.business.model.SendChatMessageCommand;
import com.opendatajungle.conversation.api.business.repository.ConversationMessageRepository;
import com.opendatajungle.conversation.api.business.repository.ConversationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationServiceImplTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private ConversationMessageRepository conversationMessageRepository;

    @Mock
    private AuthenticationUseCase authenticationService;

    @Mock
    private ChatService chatService;

    @InjectMocks
    private ConversationServiceImpl conversationService;

    @Test
    void create_shouldSaveNewConversationWithGeneratedIdAndCurrentUser() {
        // Given
        when(authenticationService.getCurrentUser()).thenReturn("user-1");
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Conversation result = conversationService.create("My title", "You are helpful");

        // Then
        ArgumentCaptor<Conversation> captor = ArgumentCaptor.forClass(Conversation.class);
        verify(conversationRepository).save(captor.capture());
        Conversation saved = captor.getValue();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo("user-1");
        assertThat(saved.getTitle()).isEqualTo("My title");
        assertThat(saved.getSystemMessage()).isEqualTo("You are helpful");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getLastMessageAt()).isEqualTo(saved.getCreatedAt());
        assertThat(result).isSameAs(saved);
    }

    @Test
    void findById_shouldReturnConversation_whenFoundForCurrentUser() {
        // Given
        UUID conversationId = UUID.randomUUID();
        Conversation conversation = new Conversation(conversationId, "user-1", "title", "system", Instant.now(), Instant.now());
        when(authenticationService.getCurrentUser()).thenReturn("user-1");
        when(conversationRepository.findByIdAndUserId(conversationId, "user-1")).thenReturn(Optional.of(conversation));

        // When
        Conversation result = conversationService.findById(conversationId);

        // Then
        assertThat(result).isEqualTo(conversation);
    }

    @Test
    void findById_shouldThrowNotFoundException_whenConversationNotFoundForCurrentUser() {
        // Given
        UUID conversationId = UUID.randomUUID();
        when(authenticationService.getCurrentUser()).thenReturn("user-1");
        when(conversationRepository.findByIdAndUserId(conversationId, "user-1")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> conversationService.findById(conversationId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Conversation not found with id: " + conversationId);
    }

    @Test
    void findAllByUser_shouldReturnConversationsForCurrentUser() {
        // Given
        Conversation conversation = new Conversation(UUID.randomUUID(), "user-1", "title", "system", Instant.now(), Instant.now());
        when(authenticationService.getCurrentUser()).thenReturn("user-1");
        when(conversationRepository.findAllByUserId("user-1")).thenReturn(List.of(conversation));

        // When
        List<Conversation> result = conversationService.findAllByUser();

        // Then
        assertThat(result).containsExactly(conversation);
    }

    @Test
    void findAll_shouldReturnAllConversations() {
        // Given
        Conversation conversation = new Conversation(UUID.randomUUID(), "user-1", "title", "system", Instant.now(), Instant.now());
        when(conversationRepository.findAll()).thenReturn(List.of(conversation));

        // When
        List<Conversation> result = conversationService.findAll();

        // Then
        assertThat(result).containsExactly(conversation);
    }

    @Test
    void update_shouldUpdateTitleAndSystemMessage_whenBothProvided() {
        // Given
        UUID conversationId = UUID.randomUUID();
        Conversation conversation = new Conversation(conversationId, "user-1", "old title", "old system", Instant.now(), Instant.now());
        when(authenticationService.getCurrentUser()).thenReturn("user-1");
        when(conversationRepository.findByIdAndUserId(conversationId, "user-1")).thenReturn(Optional.of(conversation));
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Conversation result = conversationService.update(conversationId, "new title", "new system");

        // Then
        assertThat(result.getTitle()).isEqualTo("new title");
        assertThat(result.getSystemMessage()).isEqualTo("new system");
        assertThat(result.getLastMessageAt()).isNotNull();
    }

    @Test
    void update_shouldKeepExistingTitle_whenTitleIsNull() {
        // Given
        UUID conversationId = UUID.randomUUID();
        Conversation conversation = new Conversation(conversationId, "user-1", "old title", "old system", Instant.now(), Instant.now());
        when(authenticationService.getCurrentUser()).thenReturn("user-1");
        when(conversationRepository.findByIdAndUserId(conversationId, "user-1")).thenReturn(Optional.of(conversation));
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Conversation result = conversationService.update(conversationId, null, "new system");

        // Then
        assertThat(result.getTitle()).isEqualTo("old title");
        assertThat(result.getSystemMessage()).isEqualTo("new system");
    }

    @Test
    void update_shouldKeepExistingSystemMessage_whenSystemMessageIsBlank() {
        // Given
        UUID conversationId = UUID.randomUUID();
        Conversation conversation = new Conversation(conversationId, "user-1", "old title", "old system", Instant.now(), Instant.now());
        when(authenticationService.getCurrentUser()).thenReturn("user-1");
        when(conversationRepository.findByIdAndUserId(conversationId, "user-1")).thenReturn(Optional.of(conversation));
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Conversation result = conversationService.update(conversationId, "new title", "   ");

        // Then
        assertThat(result.getTitle()).isEqualTo("new title");
        assertThat(result.getSystemMessage()).isEqualTo("old system");
    }

    @Test
    void update_shouldThrowNotFoundException_whenConversationNotFound() {
        // Given
        UUID conversationId = UUID.randomUUID();
        when(authenticationService.getCurrentUser()).thenReturn("user-1");
        when(conversationRepository.findByIdAndUserId(conversationId, "user-1")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> conversationService.update(conversationId, "new title", "new system"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteByIds_shouldThrowParamException_whenIdsIsNull() {
        // When & Then
        assertThatThrownBy(() -> conversationService.deleteByIds(null))
                .isInstanceOf(ParamException.class)
                .hasMessage("At least one conversation ID must be provided");
    }

    @Test
    void deleteByIds_shouldThrowParamException_whenIdsIsEmpty() {
        // When & Then
        assertThatThrownBy(() -> conversationService.deleteByIds(List.of()))
                .isInstanceOf(ParamException.class)
                .hasMessage("At least one conversation ID must be provided");
    }

    @Test
    void deleteByIds_shouldDeleteAllIds_whenAllBelongToCurrentUser() {
        // Given
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Conversation conversation1 = new Conversation(id1, "user-1", "title1", "system1", Instant.now(), Instant.now());
        Conversation conversation2 = new Conversation(id2, "user-1", "title2", "system2", Instant.now(), Instant.now());
        when(authenticationService.getCurrentUser()).thenReturn("user-1");
        when(conversationRepository.findByIdAndUserId(id1, "user-1")).thenReturn(Optional.of(conversation1));
        when(conversationRepository.findByIdAndUserId(id2, "user-1")).thenReturn(Optional.of(conversation2));

        // When
        conversationService.deleteByIds(List.of(id1, id2));

        // Then
        verify(conversationRepository).delete(List.of(id1, id2));
    }

    @Test
    void deleteByIds_shouldThrowNotFoundExceptionAndNotDelete_whenOneIdDoesNotBelongToCurrentUser() {
        // Given
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Conversation conversation1 = new Conversation(id1, "user-1", "title1", "system1", Instant.now(), Instant.now());
        when(authenticationService.getCurrentUser()).thenReturn("user-1");
        when(conversationRepository.findByIdAndUserId(id1, "user-1")).thenReturn(Optional.of(conversation1));
        when(conversationRepository.findByIdAndUserId(id2, "user-1")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> conversationService.deleteByIds(List.of(id1, id2)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Conversation not found with id: " + id2);
        verify(conversationRepository, never()).delete(any());
    }

    @Test
    void chat_shouldThrowParamException_whenMessageIsBlank() {
        // Given
        SendChatMessageCommand command = new SendChatMessageCommand(UUID.randomUUID(), "   ", List.of(), Set.of(), "gpt-4");

        // When & Then
        assertThatThrownBy(() -> conversationService.chat(command))
                .isInstanceOf(ParamException.class)
                .hasMessage("Message and conversation ID must be provided");
    }

    @Test
    void chat_shouldThrowParamException_whenConversationIdIsNull() {
        // Given
        SendChatMessageCommand command = new SendChatMessageCommand(null, "hello", List.of(), Set.of(), "gpt-4");

        // When & Then
        assertThatThrownBy(() -> conversationService.chat(command))
                .isInstanceOf(ParamException.class)
                .hasMessage("Message and conversation ID must be provided");
    }

    @Test
    void chat_shouldDelegateToChatServiceAndUpdateLastMessageAt_whenCommandIsValid() {
        // Given
        UUID conversationId = UUID.randomUUID();
        SendChatMessageCommand command = new SendChatMessageCommand(conversationId, "hello", List.of(), Set.of(), "gpt-4");
        Conversation conversation = new Conversation(conversationId, "user-1", "title", "system message", Instant.now(), Instant.now());
        ChatResult chatResult = new ChatResult("hi there", List.of());
        when(authenticationService.getCurrentUser()).thenReturn("user-1");
        when(conversationRepository.findByIdAndUserId(conversationId, "user-1")).thenReturn(Optional.of(conversation));
        when(chatService.chat(command, "system message")).thenReturn(chatResult);

        // When
        ChatResult result = conversationService.chat(command);

        // Then
        assertThat(result).isEqualTo(chatResult);
        verify(conversationRepository).updateLastMessageAtToNow(conversation);
    }

    @Test
    void getMessages_shouldReturnMessages_whenConversationExists() {
        // Given
        UUID conversationId = UUID.randomUUID();
        Conversation conversation = new Conversation(conversationId, "user-1", "title", "system", Instant.now(), Instant.now());
        ConversationMessage message = new ConversationMessage(UUID.randomUUID(), conversationId, "USER", "hello", Instant.now(), List.of());
        when(authenticationService.getCurrentUser()).thenReturn("user-1");
        when(conversationRepository.findByIdAndUserId(conversationId, "user-1")).thenReturn(Optional.of(conversation));
        when(conversationMessageRepository.findAllByConversationId(conversationId)).thenReturn(List.of(message));

        // When
        List<ConversationMessage> result = conversationService.getMessages(conversationId);

        // Then
        assertThat(result).containsExactly(message);
    }

    @Test
    void getMessages_shouldThrowNotFoundException_whenConversationDoesNotExist() {
        // Given
        UUID conversationId = UUID.randomUUID();
        when(authenticationService.getCurrentUser()).thenReturn("user-1");
        when(conversationRepository.findByIdAndUserId(conversationId, "user-1")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> conversationService.getMessages(conversationId))
                .isInstanceOf(NotFoundException.class);
        verify(conversationMessageRepository, never()).findAllByConversationId(any());
    }
}
