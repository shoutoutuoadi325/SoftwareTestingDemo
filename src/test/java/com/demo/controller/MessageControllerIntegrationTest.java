package com.demo.controller;

import com.demo.dao.MessageDao;
import com.demo.dao.UserDao;
import com.demo.entity.Message;
import com.demo.exception.LoginException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MessageControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MessageDao messageDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ObjectMapper objectMapper;

    private MockHttpSession userSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", userDao.findByUserID("test"));
        return session;
    }

    private Message createMessage(String userId, int state, String content) {
        Message message = new Message();
        message.setUserID(userId);
        message.setState(state);
        message.setContent(content);
        message.setTime(LocalDateTime.now());
        return messageDao.save(message);
    }

    @Test
    @Tag("P0")
    void itMsg01_messageListWithSession_shouldReturnViewAndModels() throws Exception {
        mockMvc.perform(get("/message_list").session(userSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("message_list"))
                .andExpect(model().attributeExists("total"))
                .andExpect(model().attributeExists("user_total"));
    }

    @Test
    @Tag("P0")
    void itMsg02_messageListWithoutSession_shouldThrowLoginException() throws Exception {
        mockMvc.perform(get("/message_list"))
                .andExpect(status().is5xxServerError())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof LoginException));
    }

    @Test
    @Tag("P0")
    void itMsg03_getPublicMessageList_shouldReturnOnlyPassStateMessages() throws Exception {
        String json = mockMvc.perform(get("/message/getMessageList").param("page", "1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode array = objectMapper.readTree(json);
        assertTrue(array.isArray());
        Iterator<JsonNode> it = array.elements();
        while (it.hasNext()) {
            JsonNode node = it.next();
            assertEquals(2, node.get("state").asInt());
        }
    }

    @Test
    @Tag("P0")
    void itMsg04_getUserMessageListWithSession_shouldReturnMessagesOfLoggedUser() throws Exception {
        String json = mockMvc.perform(get("/message/findUserList")
                        .param("page", "1")
                        .session(userSession()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode array = objectMapper.readTree(json);
        assertTrue(array.isArray());
        Iterator<JsonNode> it = array.elements();
        while (it.hasNext()) {
            JsonNode node = it.next();
            assertEquals("test", node.get("userID").asText());
        }
    }

    @Test
    @Tag("P0")
    void itMsg05_getUserMessageListWithoutSession_shouldThrowLoginException() throws Exception {
        mockMvc.perform(get("/message/findUserList").param("page", "1"))
                .andExpect(status().is5xxServerError())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof LoginException));
    }

    @Test
    @Tag("P0")
    void itMsg06_sendMessage_shouldInsertNoAuditMessageAndRedirect() throws Exception {
        long before = messageDao.count();
        String uniqueContent = "it_msg_" + UUID.randomUUID();

        mockMvc.perform(post("/sendMessage")
                        .param("userID", "test")
                        .param("content", uniqueContent))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/message_list"));

        long after = messageDao.count();
        assertEquals(before + 1, after);

        boolean exists = messageDao.findAll().stream()
                .anyMatch(m -> uniqueContent.equals(m.getContent())
                        && "test".equals(m.getUserID())
                        && m.getState() == 1);
        assertTrue(exists);
    }

    @Test
    @Tag("P0")
    void itMsg07_modifyMessage_shouldReturnTrueAndResetStateToNoAudit() throws Exception {
        Message message = createMessage("test", 2, "old_content");

        mockMvc.perform(post("/modifyMessage.do")
                        .param("messageID", String.valueOf(message.getMessageID()))
                        .param("content", "new_content"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        Message updated = messageDao.findByMessageID(message.getMessageID());
        assertNotNull(updated);
        assertEquals("new_content", updated.getContent());
        assertEquals(1, updated.getState());
    }

    @Test
    @Tag("P0")
    void itMsg08_deleteMessage_shouldReturnTrueAndDeleteRecord() throws Exception {
        Message message = createMessage("test", 1, "to_delete");

        mockMvc.perform(post("/delMessage.do")
                        .param("messageID", String.valueOf(message.getMessageID())))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        assertEquals(null, messageDao.findByMessageID(message.getMessageID()));
    }
}
