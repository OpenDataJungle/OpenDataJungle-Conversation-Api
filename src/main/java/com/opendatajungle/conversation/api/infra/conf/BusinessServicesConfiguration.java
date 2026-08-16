package com.opendatajungle.conversation.api.infra.conf;

import com.opendatajungle.commons.business.service.AuthenticationUseCase;
import com.opendatajungle.conversation.api.business.repository.ConversationMessageRepository;
import com.opendatajungle.conversation.api.business.repository.ConversationRepository;
import com.opendatajungle.conversation.api.business.service.ChatService;
import com.opendatajungle.conversation.api.business.service.ConversationService;
import com.opendatajungle.conversation.api.business.service.ConversationServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BusinessServicesConfiguration {

    @Bean
    @ConditionalOnMissingBean(ConversationService.class)
    public ConversationService conversationService(
            ConversationRepository conversationRepository,
            ConversationMessageRepository conversationMessageRepository,
            AuthenticationUseCase authenticationService,
            ChatService chatService) {
        return new ConversationServiceImpl(
                conversationRepository,
                conversationMessageRepository,
                authenticationService,
                chatService);
    }
}
