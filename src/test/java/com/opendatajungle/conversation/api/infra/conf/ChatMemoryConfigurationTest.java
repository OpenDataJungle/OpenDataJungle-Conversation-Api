package com.opendatajungle.conversation.api.infra.conf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ChatMemoryConfigurationTest {

    @Mock
    private ChatMemoryRepository chatMemoryRepository;

    private final ChatMemoryConfiguration configuration = new ChatMemoryConfiguration();

    @Test
    void chatMemory_shouldReturnMessageWindowChatMemoryBackedByProvidedRepository() {
        // Given & When
        ChatMemory result = configuration.chatMemory(chatMemoryRepository);

        // Then
        assertThat(result).isInstanceOf(MessageWindowChatMemory.class);
    }
}
