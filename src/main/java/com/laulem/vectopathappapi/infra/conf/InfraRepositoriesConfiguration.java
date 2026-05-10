package com.laulem.vectopathappapi.infra.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laulem.vectopathappapi.business.repository.ConversationMessageRepository;
import com.laulem.vectopathappapi.business.repository.ConversationRepository;
import com.laulem.vectopathappapi.infra.properties.ChatProperties;
import com.laulem.vectopathappapi.infra.repository.ChatMemoryRepositoryImpl;
import com.laulem.vectopathappapi.infra.repository.ConversationJpaRepository;
import com.laulem.vectopathappapi.infra.repository.ConversationMessageJpaRepository;
import com.laulem.vectopathappapi.infra.repository.ConversationMessageRepositoryImpl;
import com.laulem.vectopathappapi.infra.repository.ConversationRepositoryImpl;
import com.laulem.vectopathappapi.infra.tool.ChatRequestHolder;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfraRepositoriesConfiguration {

    @Bean
    @ConditionalOnMissingBean(ConversationRepository.class)
    public ConversationRepository conversationRepository(ConversationJpaRepository conversationJpaRepository) {
        return new ConversationRepositoryImpl(conversationJpaRepository);
    }

    @Bean
    @ConditionalOnMissingBean(ConversationMessageRepository.class)
    public ConversationMessageRepository conversationMessageRepository(
            ConversationMessageJpaRepository conversationMessageJpaRepository,
            ObjectMapper objectMapper) {
        return new ConversationMessageRepositoryImpl(conversationMessageJpaRepository, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean(ChatMemoryRepository.class)
    public ChatMemoryRepository chatMemoryRepository(
            ChatProperties chatProperties,
            ConversationMessageJpaRepository conversationMessageJpaRepository,
            ChatRequestHolder chatRequestHolder,
            ObjectMapper objectMapper) {
        return new ChatMemoryRepositoryImpl(chatProperties, conversationMessageJpaRepository, chatRequestHolder, objectMapper);
    }
}

