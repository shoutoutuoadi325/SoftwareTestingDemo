package com.demo.service;

import com.demo.dao.MessageDao;
import com.demo.entity.Message;
import com.demo.service.impl.MessageServiceImpl;
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
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void utMs01_findById_shouldReturnMessage() {
        Message message = buildMessage(1, "u1001", MessageService.STATE_NO_AUDIT);
        when(messageDao.getOne(1)).thenReturn(message);

        Message result = messageService.findById(1);

        assertEquals(1, result.getMessageID());
        verify(messageDao).getOne(1);
    }

    @Test
    void utMs02_findByUser_shouldReturnUserMessages() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Message> page = new PageImpl<>(Collections.singletonList(buildMessage(1, "u1001", MessageService.STATE_PASS)));
        when(messageDao.findAllByUserID("u1001", pageable)).thenReturn(page);

        Page<Message> result = messageService.findByUser("u1001", pageable);

        assertEquals(1, result.getTotalElements());
        verify(messageDao).findAllByUserID("u1001", pageable);
    }

    @Test
    void utMs03_create_shouldReturnMessageID() {
        Message message = buildMessage(8, "u1001", MessageService.STATE_NO_AUDIT);
        when(messageDao.save(message)).thenReturn(message);

        int messageID = messageService.create(message);

        assertEquals(8, messageID);
        verify(messageDao).save(message);
    }

    @Test
    void utMs04_update_shouldSaveMessage() {
        Message message = buildMessage(2, "u1001", MessageService.STATE_NO_AUDIT);

        messageService.update(message);

        verify(messageDao).save(message);
    }

    @Test
    void utMs05_delById_shouldDeleteMessage() {
        messageService.delById(4);

        verify(messageDao).deleteById(4);
    }

    @Test
    void utMs06_confirmMessage_shouldUpdateStateToPass() {
        Message message = buildMessage(10, "u1001", MessageService.STATE_NO_AUDIT);
        when(messageDao.findByMessageID(10)).thenReturn(message);

        messageService.confirmMessage(10);

        verify(messageDao).updateState(MessageService.STATE_PASS, 10);
    }

    @Test
    void utMs07_confirmMessage_notFound_shouldThrowRuntimeException() {
        when(messageDao.findByMessageID(999)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> messageService.confirmMessage(999));

        assertEquals("留言不存在", ex.getMessage());
    }

    @Test
    void utMs08_rejectMessage_shouldUpdateStateToReject() {
        Message message = buildMessage(11, "u1001", MessageService.STATE_NO_AUDIT);
        when(messageDao.findByMessageID(11)).thenReturn(message);

        messageService.rejectMessage(11);

        verify(messageDao).updateState(MessageService.STATE_REJECT, 11);
    }

    @Test
    void utMs09_rejectMessage_notFound_shouldThrowRuntimeException() {
        when(messageDao.findByMessageID(998)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> messageService.rejectMessage(998));

        assertEquals("留言不存在", ex.getMessage());
    }

    @Test
    void utMs10_findWaitState_shouldReturnNoAuditPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Message> page = new PageImpl<>(Collections.singletonList(buildMessage(3, "u1001", MessageService.STATE_NO_AUDIT)));
        when(messageDao.findAllByState(MessageService.STATE_NO_AUDIT, pageable)).thenReturn(page);

        Page<Message> result = messageService.findWaitState(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(MessageService.STATE_NO_AUDIT, result.getContent().get(0).getState());
        verify(messageDao).findAllByState(MessageService.STATE_NO_AUDIT, pageable);
    }

    @Test
    void utMs11_findPassState_shouldReturnPassPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Message> page = new PageImpl<>(Collections.singletonList(buildMessage(5, "u1001", MessageService.STATE_PASS)));
        when(messageDao.findAllByState(MessageService.STATE_PASS, pageable)).thenReturn(page);

        Page<Message> result = messageService.findPassState(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(MessageService.STATE_PASS, result.getContent().get(0).getState());
        verify(messageDao).findAllByState(MessageService.STATE_PASS, pageable);
    }

    private Message buildMessage(int id, String userID, int state) {
        Message message = new Message();
        message.setMessageID(id);
        message.setUserID(userID);
        message.setContent("msg-" + id);
        message.setTime(LocalDateTime.of(2026, 4, 23, 12, 0, 0));
        message.setState(state);
        return message;
    }
}
