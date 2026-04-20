package com.demo.service;

import com.demo.dao.UserDao;
import com.demo.entity.User;
import com.demo.service.impl.UserServiceImpl;
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

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @Tag("P0")
    void utUs01_findByUserID_shouldReturnUser() {
        User user = new User(1, "test", "test", "test", "", "", 0, "");
        when(userDao.findByUserID("test")).thenReturn(user);

        User result = userService.findByUserID("test");

        assertSame(user, result);
    }

    @Test
    @Tag("P1")
    void utUs02_findByUserID_notExists_shouldReturnNull() {
        when(userDao.findByUserID("not_exists")).thenReturn(null);

        User result = userService.findByUserID("not_exists");

        assertNull(result);
    }

    @Test
    @Tag("P0")
    void utUs03_findById_shouldReturnUser() {
        User user = new User(1, "test", "test", "test", "", "", 0, "");
        when(userDao.findById(1)).thenReturn(user);

        User result = userService.findById(1);

        assertSame(user, result);
    }

    @Test
    @Tag("P0")
    void utUs04_findByUserIDPageable_shouldReturnOnlyNormalUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(Arrays.asList(
                new User(1, "u1", "u1", "p", "", "", 0, ""),
                new User(2, "u2", "u2", "p", "", "", 0, "")
        ));
        when(userDao.findAllByIsadmin(0, pageable)).thenReturn(page);

        Page<User> result = userService.findByUserID(pageable);

        assertEquals(2, result.getContent().size());
        result.getContent().forEach(user -> assertEquals(0, user.getIsadmin()));
    }

    @Test
    @Tag("P0")
    void utUs05_checkLogin_success_shouldReturnUser() {
        User user = new User(1, "test", "test", "test", "", "", 0, "");
        when(userDao.findByUserIDAndPassword("test", "test")).thenReturn(user);

        User result = userService.checkLogin("test", "test");

        assertSame(user, result);
    }

    @Test
    @Tag("P0")
    void utUs06_checkLogin_fail_shouldReturnNull() {
        when(userDao.findByUserIDAndPassword("test", "wrong")).thenReturn(null);

        User result = userService.checkLogin("test", "wrong");

        assertNull(result);
    }

    @Test
    @Tag("P0")
    void utUs07_create_shouldReturnTotalCountAfterSave() {
        User toCreate = new User();
        when(userDao.findAll()).thenReturn(Arrays.asList(new User(), new User(), new User()));

        int result = userService.create(toCreate);

        verify(userDao).save(toCreate);
        assertEquals(3, result);
    }

    @Test
    @Tag("P0")
    void utUs08_updateUser_shouldSaveEntity() {
        User user = new User(1, "test", "newName", "p", "a@b.com", "123", 0, "");

        userService.updateUser(user);

        verify(userDao).save(user);
    }

    @Test
    @Tag("P0")
    void utUs09_delByID_shouldDeleteById() {
        userService.delByID(10);

        verify(userDao).deleteById(10);
    }

    @Test
    @Tag("P1")
    void utUs10_countUserID_shouldReturnCount() {
        when(userDao.countByUserID("test")).thenReturn(1);
        when(userDao.countByUserID("ut_new")).thenReturn(0);

        int existing = userService.countUserID("test");
        int notExisting = userService.countUserID("ut_new");

        assertEquals(1, existing);
        assertEquals(0, notExisting);
    }
}
