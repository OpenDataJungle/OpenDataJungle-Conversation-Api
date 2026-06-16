package com.laulem.vectopath.conversation.api.infra.conf;

import com.laulem.vectopath.conversation.api.business.service.AuthenticationService;
import com.laulem.vectopath.conversation.api.business.service.ChatService;
import com.laulem.vectopath.conversation.api.business.repository.ConversationMessageRepository;
import com.laulem.vectopath.conversation.api.business.repository.ConversationRepository;
import com.laulem.vectopath.conversation.api.business.service.ConversationService;
import com.laulem.vectopath.conversation.api.business.service.ConversationServiceImpl;
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
            AuthenticationService authenticationService,
            ChatService chatService) {
        return new ConversationServiceImpl(
                conversationRepository,
                conversationMessageRepository,
                authenticationService,
                chatService);
    }
}
