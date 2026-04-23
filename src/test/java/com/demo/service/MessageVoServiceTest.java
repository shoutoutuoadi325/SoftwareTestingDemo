package com.demo.service;

import com.demo.dao.MessageDao;
import com.demo.dao.UserDao;
import com.demo.entity.Message;
import com.demo.entity.User;
import com.demo.entity.vo.MessageVo;
import com.demo.service.impl.MessageVoServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageVoServiceTest {

    @Mock
    private MessageDao messageDao;

    @Mock
    private UserDao userDao;

    @InjectMocks
    private MessageVoServiceImpl messageVoService;

    @Test
    void utMvs01_returnMessageVoByMessageID_shouldMapUserInfo() {
        Message message = buildMessage(1, "u1001", MessageService.STATE_PASS);
        User user = buildUser("u1001", "alice");
        when(messageDao.findByMessageID(1)).thenReturn(message);
        when(userDao.findByUserID("u1001")).thenReturn(user);

        MessageVo result = messageVoService.returnMessageVoByMessageID(1);

        assertEquals(1, result.getMessageID());
        assertEquals("u1001", result.getUserID());
        assertEquals("alice", result.getUserName());
        assertEquals("avatar.png", result.getPicture());
    }

    @Test
    void utMvs02_returnVo_shouldReturnMappedListWithSameSize() {
        Message message1 = buildMessage(1, "u1001", MessageService.STATE_PASS);
        Message message2 = buildMessage(2, "u1002", MessageService.STATE_NO_AUDIT);

        when(messageDao.findByMessageID(1)).thenReturn(message1);
        when(messageDao.findByMessageID(2)).thenReturn(message2);
        when(userDao.findByUserID("u1001")).thenReturn(buildUser("u1001", "alice"));
        when(userDao.findByUserID("u1002")).thenReturn(buildUser("u1002", "bob"));

        List<MessageVo> result = messageVoService.returnVo(Arrays.asList(message1, message2));

        assertEquals(2, result.size());
        assertEquals("alice", result.get(0).getUserName());
        assertEquals("bob", result.get(1).getUserName());
    }

    @Test
    void utMvs03_returnMessageVoByMessageID_whenMessageMissing_shouldThrowIllegalArgumentException() {
        when(messageDao.findByMessageID(404)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> messageVoService.returnMessageVoByMessageID(404));
    }

    private Message buildMessage(int id, String userID, int state) {
        Message message = new Message();
        message.setMessageID(id);
        message.setUserID(userID);
        message.setContent("content-" + id);
        message.setState(state);
        message.setTime(LocalDateTime.of(2026, 4, 23, 10, 0, 0));
        return message;
    }

    private User buildUser(String userID, String userName) {
        User user = new User();
        user.setUserID(userID);
        user.setUserName(userName);
        user.setPicture("avatar.png");
        return user;
    }
}
