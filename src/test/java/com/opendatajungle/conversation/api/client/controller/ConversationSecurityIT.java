package com.opendatajungle.conversation.api.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opendatajungle.conversation.api.testconfig.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("security-it")
@Transactional
@Import(TestcontainersConfiguration.class)
class ConversationSecurityIT {

    private static final String CONVERSATIONS_PATH = "/api/v1/conversations";
    private static final String READ_SCOPE = "conversations.read";
    private static final String WRITE_SCOPE = "conversations.write";
    private static final String DELETE_SCOPE = "conversations.delete";
    private static final String ADMIN_SCOPE = "conversations.admin";

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private MockHttpServletRequestBuilder asUser(MockHttpServletRequestBuilder builder, String username, String... scopes) {
        SimpleGrantedAuthority[] authorities = List.of(scopes).stream()
                .map(SimpleGrantedAuthority::new)
                .toArray(SimpleGrantedAuthority[]::new);
        return builder.with(jwt()
                .jwt(token -> token.claim("preferred_username", username))
                .authorities(authorities));
    }

    @Test
    void actuatorHealth_shouldBePubliclyAccessible_withoutAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void create_shouldReturn401_whenUnauthenticated() throws Exception {
        mockMvc.perform(post(CONVERSATIONS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Trip planning","system_message":null}"""))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_shouldReturn403_whenMissingWriteScope() throws Exception {
        mockMvc.perform(asUser(post(CONVERSATIONS_PATH), "alice", READ_SCOPE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Trip planning","system_message":null}"""))
                .andExpect(status().isForbidden());
    }

    @Test
    void listUserConversations_shouldReturn403_whenMissingReadScope() throws Exception {
        mockMvc.perform(asUser(get(CONVERSATIONS_PATH), "alice", WRITE_SCOPE))
                .andExpect(status().isForbidden());
    }

    @Test
    void chat_shouldReturn403_whenMissingWriteScope() throws Exception {
        String response = mockMvc.perform(asUser(post(CONVERSATIONS_PATH), "alice", WRITE_SCOPE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Trip planning","system_message":null}"""))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String conversationId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(asUser(post(CONVERSATIONS_PATH + "/" + conversationId + "/chat"), "alice", READ_SCOPE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"Hello there"}"""))
                .andExpect(status().isForbidden());
    }

    @Test
    void chat_shouldReturn404_whenConversationBelongsToAnotherUser() throws Exception {
        String response = mockMvc.perform(asUser(post(CONVERSATIONS_PATH), "alice", WRITE_SCOPE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Alice's chat","system_message":null}"""))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String conversationId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(asUser(post(CONVERSATIONS_PATH + "/" + conversationId + "/chat"), "bob", WRITE_SCOPE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"Hello there"}"""))
                .andExpect(status().isNotFound());
    }

    @Test
    void listAllConversations_shouldReturn403_whenMissingAdminScope() throws Exception {
        mockMvc.perform(asUser(get(CONVERSATIONS_PATH + "/admin"), "alice", READ_SCOPE))
                .andExpect(status().isForbidden());
    }

    @Test
    void listAllConversations_shouldReturn200_whenAdminScopePresent() throws Exception {
        mockMvc.perform(asUser(get(CONVERSATIONS_PATH + "/admin"), "admin-user", ADMIN_SCOPE))
                .andExpect(status().isOk());
    }

    @Test
    void bob_shouldNotSeeAlicesConversation() throws Exception {
        // Given: alice creates a conversation she owns exclusively
        String response = mockMvc.perform(asUser(post(CONVERSATIONS_PATH), "alice", WRITE_SCOPE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Alice private","system_message":null}"""))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String conversationId = objectMapper.readTree(response).get("id").asText();

        // When & Then: bob cannot read, update, or delete it — masked as 404, not 403
        mockMvc.perform(asUser(get(CONVERSATIONS_PATH + "/" + conversationId), "bob", READ_SCOPE))
                .andExpect(status().isNotFound());

        mockMvc.perform(asUser(patch(CONVERSATIONS_PATH + "/" + conversationId), "bob", WRITE_SCOPE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"hijacked","system_message":null}"""))
                .andExpect(status().isNotFound());

        mockMvc.perform(asUser(delete(CONVERSATIONS_PATH), "bob", DELETE_SCOPE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[\"" + conversationId + "\"]}"))
                .andExpect(status().isNotFound());

        // And bob's own conversation listing must not include it either
        mockMvc.perform(asUser(get(CONVERSATIONS_PATH), "bob", READ_SCOPE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id=='" + conversationId + "')]").doesNotExist());
    }

    @Test
    void deleteConversations_shouldReturn404_whenIdBelongsToAnotherUser() throws Exception {
        String response = mockMvc.perform(asUser(post(CONVERSATIONS_PATH), "alice", WRITE_SCOPE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Alice's other conversation","system_message":null}"""))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String conversationId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(asUser(delete(CONVERSATIONS_PATH), "intruder", DELETE_SCOPE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[\"" + conversationId + "\"]}"))
                .andExpect(status().isNotFound());
    }
}
