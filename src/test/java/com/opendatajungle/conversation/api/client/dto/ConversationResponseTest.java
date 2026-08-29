package com.opendatajungle.conversation.api.client.dto;

import com.opendatajungle.conversation.api.business.model.Conversation;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ConversationResponseTest {

    @Test
    void map_shouldReturnResponseWithAllFields_fromConversation() {
        // Given
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.now();
        Instant lastMessageAt = createdAt.plusSeconds(300);
        Conversation conversation = new Conversation(id, "user-1", "Trip planning", "You are a travel assistant", createdAt, lastMessageAt);

        // When
        ConversationResponse response = new ConversationResponse(conversation);

        // Then
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.userId()).isEqualTo("user-1");
        assertThat(response.title()).isEqualTo("Trip planning");
        assertThat(response.systemMessage()).isEqualTo("You are a travel assistant");
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.lastMessageAt()).isEqualTo(lastMessageAt);
    }

    @Test
    void map_shouldReturnResponseWithNullOptionalFields_whenConversationHasNulls() {
        // Given
        Conversation conversation = new Conversation(null, null, null, null, null, null);

        // When
        ConversationResponse response = new ConversationResponse(conversation);

        // Then
        assertThat(response.id()).isNull();
        assertThat(response.userId()).isNull();
        assertThat(response.title()).isNull();
        assertThat(response.systemMessage()).isNull();
        assertThat(response.createdAt()).isNull();
        assertThat(response.lastMessageAt()).isNull();
    }
}
