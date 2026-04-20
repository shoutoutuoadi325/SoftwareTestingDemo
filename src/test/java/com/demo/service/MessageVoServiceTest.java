package com.demo.service;

import com.demo.dao.MessageDao;
import com.demo.dao.UserDao;
import com.demo.entity.Message;
import com.demo.entity.User;
import com.demo.entity.vo.MessageVo;
import com.demo.service.impl.MessageVoServiceImpl;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    @Tag("P1")
    void utMvs01_returnMessageVoByMessageID_shouldMapUserInfo() {
        Message message = new Message(2, "test", "hello", LocalDateTime.now(), 2);
        User user = new User(1, "test", "测试用户", "p", "", "", 0, "pic.png");
        when(messageDao.findByMessageID(2)).thenReturn(message);
        when(userDao.findByUserID("test")).thenReturn(user);

        MessageVo result = messageVoService.returnMessageVoByMessageID(2);

        assertEquals(2, result.getMessageID());
        assertEquals("test", result.getUserID());
        assertEquals("测试用户", result.getUserName());
        assertEquals("pic.png", result.getPicture());
    }

    @Test
    @Tag("P1")
    void utMvs02_returnVo_shouldReturnMappedListWithSameSize() {
        Message m1 = new Message(1, "u1", "c1", LocalDateTime.now(), 2);
        Message m2 = new Message(2, "u2", "c2", LocalDateTime.now(), 1);
        when(messageDao.findByMessageID(1)).thenReturn(m1);
        when(messageDao.findByMessageID(2)).thenReturn(m2);
        when(userDao.findByUserID("u1")).thenReturn(new User(1, "u1", "n1", "p", "", "", 0, "a.png"));
        when(userDao.findByUserID("u2")).thenReturn(new User(2, "u2", "n2", "p", "", "", 0, "b.png"));

        List<MessageVo> result = messageVoService.returnVo(Arrays.asList(m1, m2));

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getMessageID());
        assertEquals(2, result.get(1).getMessageID());
    }
}
