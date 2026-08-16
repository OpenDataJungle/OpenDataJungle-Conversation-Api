package com.opendatajungle.conversation.api.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opendatajungle.conversation.api.business.model.ChatResult;
import com.opendatajungle.conversation.api.business.service.ChatService;
import com.opendatajungle.conversation.api.testconfig.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser
@Transactional
@Import(TestcontainersConfiguration.class)
class ConversationApiIT {

    private static final String CONVERSATIONS_PATH = "/api/v1/conversations";
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private MockMvc mockMvc;
    /**
     * The chat endpoint delegates to an LLM after the conversation/scope checks; mocking only this
     * port keeps the rest of the stack (controller, security, persistence) real.
     */
    @MockitoBean
    private ChatService chatService;

    @Test
    void create_shouldCreateConversation_whenValidRequest() throws Exception {
        mockMvc.perform(post(CONVERSATIONS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Trip planning","system_message":"You are a travel assistant"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Trip planning"))
                .andExpect(jsonPath("$.system_message").value("You are a travel assistant"))
                .andExpect(jsonPath("$.user_id").value("anonymous"));
    }

    @Test
    void create_shouldReturnValidationError_whenTitleExceedsMaxSize() throws Exception {
        String tooLongTitle = "a".repeat(501);

        mockMvc.perform(post(CONVERSATIONS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", tooLongTitle))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void fullLifecycle_createGetListUpdateDelete_shouldSucceed() throws Exception {
        // Create
        String createBody = mockMvc.perform(post(CONVERSATIONS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Trip planning","system_message":"You are a travel assistant"}"""))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String conversationId = objectMapper.readTree(createBody).get("id").asText();

        // Get
        mockMvc.perform(get(CONVERSATIONS_PATH + "/{id}", conversationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Trip planning"));

        // List
        mockMvc.perform(get(CONVERSATIONS_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id=='" + conversationId + "')]").exists());

        // Update
        mockMvc.perform(patch(CONVERSATIONS_PATH + "/{id}", conversationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Updated title","system_message":"Updated system message"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.system_message").value("Updated system message"));

        // Delete
        mockMvc.perform(delete(CONVERSATIONS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[\"" + conversationId + "\"]}"))
                .andExpect(status().isNoContent());

        // Get after delete -> not found
        mockMvc.perform(get(CONVERSATIONS_PATH + "/{id}", conversationId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void getConversation_shouldReturnNotFound_whenConversationDoesNotExist() throws Exception {
        mockMvc.perform(get(CONVERSATIONS_PATH + "/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void updateConversation_shouldReturnNotFound_whenConversationDoesNotExist() throws Exception {
        mockMvc.perform(patch(CONVERSATIONS_PATH + "/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"New title","system_message":null}"""))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteConversations_shouldReturnNotFound_whenIdDoesNotExist() throws Exception {
        mockMvc.perform(delete(CONVERSATIONS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[\"" + UUID.randomUUID() + "\"]}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMessages_shouldReturnEmptyList_whenConversationHasNoMessages() throws Exception {
        String createBody = mockMvc.perform(post(CONVERSATIONS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Empty conversation","system_message":null}"""))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String conversationId = objectMapper.readTree(createBody).get("id").asText();

        mockMvc.perform(get(CONVERSATIONS_PATH + "/{id}/messages", conversationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getMessages_shouldReturnNotFound_whenConversationDoesNotExist() throws Exception {
        mockMvc.perform(get(CONVERSATIONS_PATH + "/{id}/messages", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void chat_shouldReturnReply_whenConversationExists() throws Exception {
        String createBody = mockMvc.perform(post(CONVERSATIONS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Trip planning","system_message":"You are a travel assistant"}"""))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String conversationId = objectMapper.readTree(createBody).get("id").asText();

        when(chatService.chat(any(), any())).thenReturn(new ChatResult("Hi, how can I help?", List.of()));

        mockMvc.perform(post(CONVERSATIONS_PATH + "/{id}/chat", conversationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"Hello there"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value("Hi, how can I help?"));
    }

    @Test
    void chat_shouldReturnNotFound_whenConversationDoesNotExist() throws Exception {
        mockMvc.perform(post(CONVERSATIONS_PATH + "/{id}/chat", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"Hello there"}"""))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void chat_shouldReturnValidationError_whenMessageIsBlank() throws Exception {
        String createBody = mockMvc.perform(post(CONVERSATIONS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Trip planning","system_message":null}"""))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String conversationId = objectMapper.readTree(createBody).get("id").asText();

        mockMvc.perform(post(CONVERSATIONS_PATH + "/{id}/chat", conversationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"   "}"""))
                .andExpect(status().isBadRequest());
    }
}
