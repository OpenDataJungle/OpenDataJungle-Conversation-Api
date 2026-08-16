package com.opendatajungle.conversation.api.infra.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opendatajungle.conversation.api.business.repository.ConversationMessageRepository;
import com.opendatajungle.conversation.api.business.repository.ConversationRepository;
import com.opendatajungle.conversation.api.infra.properties.ChatProperties;
import com.opendatajungle.conversation.api.infra.repository.ChatMemoryRepositoryImpl;
import com.opendatajungle.conversation.api.infra.repository.ConversationJpaRepository;
import com.opendatajungle.conversation.api.infra.repository.ConversationMessageJpaRepository;
import com.opendatajungle.conversation.api.infra.repository.ConversationMessageRepositoryImpl;
import com.opendatajungle.conversation.api.infra.repository.ConversationRepositoryImpl;
import com.opendatajungle.conversation.api.infra.tool.ChatRequestHolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.memory.ChatMemoryRepository;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class InfraRepositoriesConfigurationTest {

    @Mock
    private ConversationJpaRepository conversationJpaRepository;

    @Mock
    private ConversationMessageJpaRepository conversationMessageJpaRepository;

    @Mock
    private ChatRequestHolder chatRequestHolder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final InfraRepositoriesConfiguration configuration = new InfraRepositoriesConfiguration();

    private ChatProperties buildChatProperties() {
        ChatProperties.PreProcessorsProperties.BasicResourceManagerProperties basicResourceManagerProperties =
                new ChatProperties.PreProcessorsProperties.BasicResourceManagerProperties(0, "required", "into-prompt", "too-long");
        ChatProperties.PreProcessorsProperties preProcessorsProperties = new ChatProperties.PreProcessorsProperties(
                new ChatProperties.PreProcessorsProperties.DefaultSystemPromptProperties("default prompt"),
                new ChatProperties.PreProcessorsProperties.ResourceCategorizationProperties("categorizer prompt"),
                basicResourceManagerProperties);
        return new ChatProperties(1000, preProcessorsProperties);
    }

    @Test
    void conversationRepository_shouldReturnConversationRepositoryImplInstance() {
        // Given & When
        ConversationRepository result = configuration.conversationRepository(conversationJpaRepository);

        // Then
        assertThat(result).isInstanceOf(ConversationRepositoryImpl.class);
    }

    @Test
    void conversationMessageRepository_shouldReturnConversationMessageRepositoryImplInstance() {
        // Given & When
        ConversationMessageRepository result = configuration.conversationMessageRepository(conversationMessageJpaRepository, objectMapper);

        // Then
        assertThat(result).isInstanceOf(ConversationMessageRepositoryImpl.class);
    }

    @Test
    void chatMemoryRepository_shouldReturnChatMemoryRepositoryImplInstance() {
        // Given & When
        ChatMemoryRepository result = configuration.chatMemoryRepository(
                buildChatProperties(), conversationMessageJpaRepository, chatRequestHolder, objectMapper);

        // Then
        assertThat(result).isInstanceOf(ChatMemoryRepositoryImpl.class);
    }
}
