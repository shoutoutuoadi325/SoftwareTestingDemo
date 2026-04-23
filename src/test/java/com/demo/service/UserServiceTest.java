package com.demo.service;

import com.demo.dao.UserDao;
import com.demo.entity.User;
import com.demo.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void utUs01_findByUserID_shouldReturnUser() {
        User user = buildUser(1, "u1001", "alice", 0);
        when(userDao.findByUserID("u1001")).thenReturn(user);

        User result = userService.findByUserID("u1001");

        assertEquals("u1001", result.getUserID());
        verify(userDao).findByUserID("u1001");
    }

    @Test
    void utUs02_findByUserID_notExists_shouldReturnNull() {
        when(userDao.findByUserID("missing")).thenReturn(null);

        User result = userService.findByUserID("missing");

        assertNull(result);
        verify(userDao).findByUserID("missing");
    }

    @Test
    void utUs03_findById_shouldReturnUser() {
        User user = buildUser(2, "u2001", "bob", 0);
        when(userDao.findById(2)).thenReturn(user);

        User result = userService.findById(2);

        assertEquals("u2001", result.getUserID());
        verify(userDao).findById(2);
    }

    @Test
    void utUs04_findByUserIDPageable_shouldReturnOnlyNormalUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(Collections.singletonList(buildUser(1, "u1001", "alice", 0)));
        when(userDao.findAllByIsadmin(0, pageable)).thenReturn(page);

        Page<User> result = userService.findByUserID(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(0, result.getContent().get(0).getIsadmin());
        verify(userDao).findAllByIsadmin(0, pageable);
    }

    @Test
    void utUs05_checkLogin_success_shouldReturnUser() {
        User user = buildUser(1, "u1001", "alice", 0);
        when(userDao.findByUserIDAndPassword("u1001", "123456")).thenReturn(user);

        User result = userService.checkLogin("u1001", "123456");

        assertEquals("alice", result.getUserName());
        verify(userDao).findByUserIDAndPassword("u1001", "123456");
    }

    @Test
    void utUs06_checkLogin_fail_shouldReturnNull() {
        when(userDao.findByUserIDAndPassword("u1001", "bad")).thenReturn(null);

        User result = userService.checkLogin("u1001", "bad");

        assertNull(result);
        verify(userDao).findByUserIDAndPassword("u1001", "bad");
    }

    @Test
    void utUs07_create_shouldReturnTotalCountAfterSave() {
        User user = buildUser(0, "u3001", "newUser", 0);
        List<User> allUsers = Arrays.asList(buildUser(1, "u1", "n1", 0), buildUser(2, "u2", "n2", 0), user);
        when(userDao.findAll()).thenReturn(allUsers);

        int total = userService.create(user);

        assertEquals(3, total);
        verify(userDao).save(user);
        verify(userDao).findAll();
    }

    @Test
    void utUs08_updateUser_shouldSaveEntity() {
        User user = buildUser(1, "u1001", "alice_new", 0);

        userService.updateUser(user);

        verify(userDao).save(user);
    }

    @Test
    void utUs09_delByID_shouldDeleteById() {
        userService.delByID(9);

        verify(userDao).deleteById(9);
    }

    @Test
    void utUs10_countUserID_shouldReturnCount() {
        when(userDao.countByUserID("u1001")).thenReturn(2);

        int count = userService.countUserID("u1001");

        assertEquals(2, count);
        verify(userDao).countByUserID("u1001");
    }

    private User buildUser(int id, String userID, String userName, int isAdmin) {
        User user = new User();
        user.setId(id);
        user.setUserID(userID);
        user.setUserName(userName);
        user.setPassword("123456");
        user.setEmail(userID + "@mail.com");
        user.setPhone("13000000000");
        user.setIsadmin(isAdmin);
        user.setPicture("");
        return user;
    }
}
