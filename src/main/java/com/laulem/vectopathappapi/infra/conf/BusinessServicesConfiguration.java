package com.laulem.vectopathappapi.infra.conf;

import com.laulem.vectopathappapi.business.service.AuthenticationService;
import com.laulem.vectopathappapi.business.service.ChatService;
import com.laulem.vectopathappapi.business.service.ConversationMessageRepository;
import com.laulem.vectopathappapi.business.service.ConversationRepository;
import com.laulem.vectopathappapi.business.service.ConversationService;
import com.laulem.vectopathappapi.business.service.ConversationServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BusinessServicesConfiguration {

    @Bean
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
