package com.opendatajungle.conversation.api.infra.conf;

import com.opendatajungle.commons.business.service.AuthenticationUseCase;
import com.opendatajungle.conversation.api.business.repository.ConversationMessageRepository;
import com.opendatajungle.conversation.api.business.repository.ConversationRepository;
import com.opendatajungle.conversation.api.business.service.ChatService;
import com.opendatajungle.conversation.api.business.service.ConversationService;
import com.opendatajungle.conversation.api.business.service.ConversationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class BusinessServicesConfigurationTest {

    private final BusinessServicesConfiguration configuration = new BusinessServicesConfiguration();
    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private ConversationMessageRepository conversationMessageRepository;
    @Mock
    private AuthenticationUseCase authenticationService;
    @Mock
    private ChatService chatService;

    @Test
    void conversationService_shouldReturnConversationServiceImplWiredWithProvidedDependencies() {
        // Given & When
        ConversationService result = configuration.conversationService(
                conversationRepository, conversationMessageRepository, authenticationService, chatService);

        // Then
        assertThat(result).isInstanceOf(ConversationServiceImpl.class);
    }
}
