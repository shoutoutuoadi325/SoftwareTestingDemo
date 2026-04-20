package com.demo.service;

import com.demo.dao.MessageDao;
import com.demo.entity.Message;
import com.demo.service.impl.MessageServiceImpl;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageDao messageDao;

    @InjectMocks
    private MessageServiceImpl messageService;

    @Test
    @Tag("P0")
    void utMs01_findById_shouldReturnMessage() {
        Message message = new Message(2, "test", "hello", LocalDateTime.now(), 2);
        when(messageDao.getOne(2)).thenReturn(message);

        Message result = messageService.findById(2);

        assertSame(message, result);
    }

    @Test
    @Tag("P0")
    void utMs02_findByUser_shouldReturnUserMessages() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Message> page = new PageImpl<>(Arrays.asList(
                new Message(1, "test", "c1", LocalDateTime.now(), 1),
                new Message(2, "test", "c2", LocalDateTime.now(), 2)
        ));
        when(messageDao.findAllByUserID("test", pageable)).thenReturn(page);

        Page<Message> result = messageService.findByUser("test", pageable);

        assertEquals(2, result.getContent().size());
        result.getContent().forEach(msg -> assertEquals("test", msg.getUserID()));
    }

    @Test
    @Tag("P0")
    void utMs03_create_shouldReturnMessageID() {
        Message message = new Message(0, "test", "new", LocalDateTime.now(), 1);
        Message saved = new Message(101, "test", "new", LocalDateTime.now(), 1);
        when(messageDao.save(message)).thenReturn(saved);

        int result = messageService.create(message);

        assertEquals(101, result);
    }

    @Test
    @Tag("P0")
    void utMs04_update_shouldSaveMessage() {
        Message message = new Message(2, "test", "updated", LocalDateTime.now(), 1);

        messageService.update(message);

        verify(messageDao).save(message);
    }

    @Test
    @Tag("P0")
    void utMs05_delById_shouldDeleteMessage() {
        messageService.delById(2);

        verify(messageDao).deleteById(2);
    }

    @Test
    @Tag("P0")
    void utMs06_confirmMessage_shouldUpdateStateToPass() {
        Message message = new Message(2, "test", "c", LocalDateTime.now(), 1);
        when(messageDao.findByMessageID(2)).thenReturn(message);

        messageService.confirmMessage(2);

        verify(messageDao).updateState(MessageService.STATE_PASS, 2);
    }

    @Test
    @Tag("P0")
    void utMs07_rejectMessage_shouldUpdateStateToReject() {
        Message message = new Message(2, "test", "c", LocalDateTime.now(), 1);
        when(messageDao.findByMessageID(2)).thenReturn(message);

        messageService.rejectMessage(2);

        verify(messageDao).updateState(MessageService.STATE_REJECT, 2);
    }

    @Test
    @Tag("P0")
    void utMs08_confirmMessage_notFound_shouldThrowRuntimeException() {
        when(messageDao.findByMessageID(-1)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> messageService.confirmMessage(-1));

        assertNotNull(ex.getMessage());
        assertEquals("留言不存在", ex.getMessage());
    }

    @Test
    @Tag("P1")
    void utMs09_findWaitStateAndFindPassState_shouldReturnPagesByState() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Message> waitPage = new PageImpl<>(Arrays.asList(
                new Message(1, "u", "c", LocalDateTime.now(), MessageService.STATE_NO_AUDIT)
        ));
        Page<Message> passPage = new PageImpl<>(Arrays.asList(
                new Message(2, "u", "c", LocalDateTime.now(), MessageService.STATE_PASS)
        ));
        when(messageDao.findAllByState(MessageService.STATE_NO_AUDIT, pageable)).thenReturn(waitPage);
        when(messageDao.findAllByState(MessageService.STATE_PASS, pageable)).thenReturn(passPage);

        Page<Message> waitResult = messageService.findWaitState(pageable);
        Page<Message> passResult = messageService.findPassState(pageable);

        assertEquals(1, waitResult.getContent().size());
        assertEquals(MessageService.STATE_NO_AUDIT, waitResult.getContent().get(0).getState());
        assertEquals(1, passResult.getContent().size());
        assertEquals(MessageService.STATE_PASS, passResult.getContent().get(0).getState());
    }
}
