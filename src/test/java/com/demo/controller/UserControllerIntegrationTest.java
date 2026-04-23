package com.demo.controller;

import com.demo.controller.user.UserController;
import com.demo.entity.User;
import com.demo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class UserControllerIntegrationTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
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
    void itAuth01_getSignup_shouldReturnSignupView() throws Exception {
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"));
    }

    @Test
    void itAuth02_getLogin_shouldReturnLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void itAuth03_loginUser_shouldSetUserSessionAndReturnIndexPath() throws Exception {
        User user = buildUser("u1001", "alice", 0, "123456");
        when(userService.checkLogin("u1001", "123456")).thenReturn(user);

        mockMvc.perform(post("/loginCheck.do")
                        .param("userID", "u1001")
                        .param("password", "123456"))
                .andExpect(status().isOk())
                .andExpect(content().string("/index"));
    }

    @Test
    void itAuth04_loginAdmin_shouldSetAdminSessionAndReturnAdminIndexPath() throws Exception {
        User admin = buildUser("admin", "root", 1, "admin123");
        when(userService.checkLogin("admin", "admin123")).thenReturn(admin);

        mockMvc.perform(post("/loginCheck.do")
                        .param("userID", "admin")
                        .param("password", "admin123"))
                .andExpect(status().isOk())
                .andExpect(content().string("/admin_index"));
    }

    @Test
    void itAuth05_loginWithWrongPassword_shouldReturnFalse() throws Exception {
        when(userService.checkLogin("u1001", "bad")).thenReturn(null);

        mockMvc.perform(post("/loginCheck.do")
                        .param("userID", "u1001")
                        .param("password", "bad"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void itAuth06_register_shouldRedirectToLoginAndInsertUser() throws Exception {
        mockMvc.perform(post("/register.do")
                        .param("userID", "u2001")
                        .param("userName", "newUser")
                        .param("password", "pass")
                        .param("email", "u2001@mail.com")
                        .param("phone", "13900000000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("login"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userService).create(captor.capture());
        User created = captor.getValue();
        assertEquals("u2001", created.getUserID());
        assertEquals("newUser", created.getUserName());
        assertEquals("pass", created.getPassword());
        assertEquals("u2001@mail.com", created.getEmail());
        assertEquals("13900000000", created.getPhone());
        assertEquals("", created.getPicture());
    }

    @Test
    void itAuth07_logout_shouldClearUserSessionAndRedirectIndex() throws Exception {
        mockMvc.perform(get("/logout.do").sessionAttr("user", buildUser("u1001", "alice", 0, "123456")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"));
    }

    @Test
    void itAuth08_quit_shouldClearAdminSessionAndRedirectIndex() throws Exception {
        mockMvc.perform(get("/quit.do").sessionAttr("admin", buildUser("admin", "root", 1, "admin123")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"));
    }

    @Test
    void itAuth09_checkPasswordCorrect_shouldReturnTrue() throws Exception {
        when(userService.findByUserID("u1001")).thenReturn(buildUser("u1001", "alice", 0, "123456"));

        mockMvc.perform(get("/checkPassword.do")
                        .param("userID", "u1001")
                        .param("password", "123456"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void itAuth10_checkPasswordWrong_shouldReturnFalse() throws Exception {
        when(userService.findByUserID("u1001")).thenReturn(buildUser("u1001", "alice", 0, "123456"));

        mockMvc.perform(get("/checkPassword.do")
                        .param("userID", "u1001")
                        .param("password", "bad"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void itAuth11_checkPasswordWhenUserMissing_shouldReturnFalse() throws Exception {
        when(userService.findByUserID("missing")).thenReturn(null);

        mockMvc.perform(get("/checkPassword.do")
                        .param("userID", "missing")
                        .param("password", "whatever"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void itAuth12_updateUser_shouldRedirectAndUpdateDatabaseAndSession() throws Exception {
        User user = buildUser("u1001", "alice", 0, "oldPass");
        when(userService.findByUserID("u1001")).thenReturn(user);

        MockMultipartFile emptyPicture = new MockMultipartFile("picture", "", "application/octet-stream", new byte[0]);

        mockMvc.perform(multipart("/updateUser.do")
                        .file(emptyPicture)
                        .param("userID", "u1001")
                        .param("userName", "aliceNew")
                        .param("passwordNew", "newPass")
                        .param("email", "new@mail.com")
                        .param("phone", "13800000000")
                        .sessionAttr("user", user))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("user_info"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userService).updateUser(captor.capture());
        User updated = captor.getValue();
        assertEquals("aliceNew", updated.getUserName());
        assertEquals("newPass", updated.getPassword());
        assertEquals("new@mail.com", updated.getEmail());
        assertEquals("13800000000", updated.getPhone());
    }

    @Test
    void itAuth13_updateUserWhenPasswordEmpty_shouldKeepOldPassword() throws Exception {
        User user = buildUser("u1001", "alice", 0, "oldPass");
        when(userService.findByUserID("u1001")).thenReturn(user);

        MockMultipartFile emptyPicture = new MockMultipartFile("picture", "", "application/octet-stream", new byte[0]);

        mockMvc.perform(multipart("/updateUser.do")
                        .file(emptyPicture)
                        .param("userID", "u1001")
                        .param("userName", "aliceNew")
                        .param("passwordNew", "")
                        .param("email", "new@mail.com")
                        .param("phone", "13800000000")
                        .sessionAttr("user", user))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userService).updateUser(captor.capture());
        User updated = captor.getValue();
        assertEquals("oldPass", updated.getPassword());
    }

    @Test
    void itAuth14_userInfo_shouldReturnUserInfoView() throws Exception {
        mockMvc.perform(get("/user_info"))
                .andExpect(status().isOk())
                .andExpect(view().name("user_info"));
    }

    @Test
    void itAuth15_updateUser_userMissing_shouldReturnNotFoundInsteadOfServerError() throws Exception {
        when(userService.findByUserID("missing")).thenReturn(null);
        MockMultipartFile emptyPicture = new MockMultipartFile("picture", "", "application/octet-stream", new byte[0]);

        mockMvc.perform(multipart("/updateUser.do")
                        .file(emptyPicture)
                        .param("userID", "missing")
                        .param("userName", "any")
                        .param("passwordNew", "new")
                        .param("email", "x@mail.com")
                        .param("phone", "13800000000"))
                .andExpect(status().isNotFound());
    }

    private User buildUser(String userID, String userName, int isAdmin, String password) {
        User user = new User();
        user.setUserID(userID);
        user.setUserName(userName);
        user.setIsadmin(isAdmin);
        user.setPassword(password);
        user.setEmail(userID + "@mail.com");
        user.setPhone("13000000000");
        user.setPicture("");
        return user;
    }
}
