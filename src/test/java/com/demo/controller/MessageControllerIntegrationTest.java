package com.demo.controller;

import com.demo.controller.user.MessageController;
import com.demo.entity.Message;
import com.demo.entity.User;
import com.demo.entity.vo.MessageVo;
import com.demo.service.MessageService;
import com.demo.service.MessageVoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class MessageControllerIntegrationTest {

    @Mock
    private MessageService messageService;

    @Mock
    private MessageVoService messageVoService;

    @InjectMocks
    private MessageController messageController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(messageController)
                .setViewResolvers(viewResolver())
                .build();
    }

    private InternalResourceViewResolver viewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/templates/");
        resolver.setSuffix(".html");
        return resolver;
    }

    @Test
    void itMsg01_messageListWithSession_shouldReturnViewAndModels() throws Exception {
        Page<Message> passPage = new PageImpl<>(Collections.singletonList(buildMessage(1, "u1001", MessageService.STATE_PASS)), PageRequest.of(0, 5), 6);
        Page<Message> userPage = new PageImpl<>(Collections.singletonList(buildMessage(2, "u1001", MessageService.STATE_NO_AUDIT)), PageRequest.of(0, 5), 4);

        when(messageService.findPassState(any())).thenReturn(passPage);
        when(messageVoService.returnVo(any())).thenReturn(Collections.singletonList(buildMessageVo(1, "u1001")));
        when(messageService.findByUser(any(), any())).thenReturn(userPage);

        mockMvc.perform(get("/message_list").sessionAttr("user", buildUser("u1001")))
                .andExpect(status().isOk())
                .andExpect(view().name("message_list"))
                .andExpect(model().attributeExists("total"))
                .andExpect(model().attributeExists("user_total"));
    }

    @Test
    void itMsg02_messageListWithoutSession_shouldRedirectToLogin() throws Exception {
        when(messageService.findPassState(any())).thenReturn(new PageImpl<>(Collections.emptyList()));
        when(messageVoService.returnVo(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/message_list"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void itMsg03_getPublicMessageList_shouldReturnOnlyPassStateMessages() throws Exception {
        Page<Message> passPage = new PageImpl<>(Collections.singletonList(buildMessage(3, "u1001", MessageService.STATE_PASS)));
        when(messageService.findPassState(any())).thenReturn(passPage);
        when(messageVoService.returnVo(any())).thenReturn(Collections.singletonList(buildMessageVo(3, "u1001")));

        mockMvc.perform(get("/message/getMessageList").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"messageID\":3,\"userID\":\"u1001\"}]", false));
    }

    @Test
    void itMsg04_getUserMessageListWithSession_shouldReturnMessagesOfLoggedUser() throws Exception {
        Page<Message> userPage = new PageImpl<>(Arrays.asList(
                buildMessage(4, "u1001", MessageService.STATE_NO_AUDIT),
                buildMessage(5, "u1001", MessageService.STATE_PASS)
        ));
        when(messageService.findByUser(any(), any())).thenReturn(userPage);
        when(messageVoService.returnVo(any())).thenReturn(Arrays.asList(buildMessageVo(4, "u1001"), buildMessageVo(5, "u1001")));

        mockMvc.perform(get("/message/findUserList")
                        .param("page", "1")
                        .sessionAttr("user", buildUser("u1001")))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"messageID\":4},{\"messageID\":5}]", false));
    }

    @Test
    void itMsg05_getUserMessageListWithoutSession_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/message/findUserList").param("page", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void itMsg06_sendMessage_shouldInsertNoAuditMessageAndRedirect() throws Exception {
        mockMvc.perform(post("/sendMessage")
                        .param("userID", "u1001")
                        .param("content", "hello"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/message_list"));

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageService).create(captor.capture());
        Message created = captor.getValue();
        assertEquals("u1001", created.getUserID());
        assertEquals("hello", created.getContent());
        assertEquals(MessageService.STATE_NO_AUDIT, created.getState());
    }

    @Test
    void itMsg07_modifyMessage_shouldReturnTrueAndResetStateToNoAudit() throws Exception {
        Message message = buildMessage(6, "u1001", MessageService.STATE_PASS);
        when(messageService.findById(6)).thenReturn(message);

        mockMvc.perform(post("/modifyMessage.do")
                        .param("messageID", "6")
                        .param("content", "updated"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageService).update(captor.capture());
        Message updated = captor.getValue();
        assertEquals("updated", updated.getContent());
        assertEquals(MessageService.STATE_NO_AUDIT, updated.getState());
    }

    @Test
    void itMsg08_modifyMessageWhenTargetMissing_shouldReturnNotFound() throws Exception {
        when(messageService.findById(404)).thenReturn(null);

        mockMvc.perform(post("/modifyMessage.do")
                        .param("messageID", "404")
                        .param("content", "updated"))
                .andExpect(status().isNotFound());
    }

    @Test
    void itMsg09_deleteMessage_shouldReturnTrueAndDeleteRecord() throws Exception {
        mockMvc.perform(post("/delMessage.do").param("messageID", "9"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(messageService).delById(9);
    }

    private User buildUser(String userID) {
        User user = new User();
        user.setUserID(userID);
        user.setUserName("name");
        return user;
    }

    private Message buildMessage(int id, String userID, int state) {
        Message message = new Message();
        message.setMessageID(id);
        message.setUserID(userID);
        message.setContent("c-" + id);
        message.setState(state);
        message.setTime(LocalDateTime.of(2026, 4, 23, 11, 0, 0));
        return message;
    }

    private MessageVo buildMessageVo(int id, String userID) {
        MessageVo vo = new MessageVo();
        vo.setMessageID(id);
        vo.setUserID(userID);
        vo.setContent("c-" + id);
        vo.setState(MessageService.STATE_PASS);
        vo.setUserName("name");
        vo.setPicture("pic.png");
        vo.setTime(LocalDateTime.of(2026, 4, 23, 11, 0, 0));
        return vo;
    }
}
