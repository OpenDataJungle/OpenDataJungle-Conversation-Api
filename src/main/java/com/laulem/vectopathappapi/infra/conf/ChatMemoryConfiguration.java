package com.laulem.vectopathappapi.infra.conf;

import com.laulem.vectopathappapi.infra.repository.ChatMemoryRepositoryImpl;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatMemoryConfiguration {

    @Bean
    public ChatMemory chatMemory(ChatMemoryRepositoryImpl chatMemoryRepositoryImpl) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepositoryImpl)
                .maxMessages(Integer.MAX_VALUE)
                .build();
    }
}


