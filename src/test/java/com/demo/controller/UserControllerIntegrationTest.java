package com.demo.controller;

import com.demo.dao.UserDao;
import com.demo.entity.User;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDao userDao;

    private MockHttpSession userSession(String userId) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", userDao.findByUserID(userId));
        return session;
    }

    @Test
    @Tag("P1")
    void itAuth01_getSignup_shouldReturnSignupView() throws Exception {
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"));
    }

    @Test
    @Tag("P1")
    void itAuth02_getLogin_shouldReturnLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @Tag("P0")
    void itAuth03_loginUser_shouldSetUserSessionAndReturnIndexPath() throws Exception {
        MvcResult result = mockMvc.perform(post("/loginCheck.do")
                        .param("userID", "test")
                        .param("password", "test"))
                .andExpect(status().isOk())
                .andExpect(content().string("/index"))
                .andReturn();

        assertNotNull(result.getRequest().getSession(false));
        assertNotNull(result.getRequest().getSession(false).getAttribute("user"));
        assertNull(result.getRequest().getSession(false).getAttribute("admin"));
    }

    @Test
    @Tag("P0")
    void itAuth04_loginAdmin_shouldSetAdminSessionAndReturnAdminIndexPath() throws Exception {
        MvcResult result = mockMvc.perform(post("/loginCheck.do")
                        .param("userID", "admin")
                        .param("password", "admin"))
                .andExpect(status().isOk())
                .andExpect(content().string("/admin_index"))
                .andReturn();

        assertNotNull(result.getRequest().getSession(false));
        assertNotNull(result.getRequest().getSession(false).getAttribute("admin"));
        assertNull(result.getRequest().getSession(false).getAttribute("user"));
    }

    @Test
    @Tag("P0")
    void itAuth05_loginWithWrongPassword_shouldReturnFalse() throws Exception {
        mockMvc.perform(post("/loginCheck.do")
                        .param("userID", "test")
                        .param("password", "wrong"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @Tag("P0")
    void itAuth06_register_shouldRedirectToLoginAndInsertUser() throws Exception {
        String userId = "it_reg_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        mockMvc.perform(post("/register.do")
                        .param("userID", userId)
                        .param("userName", "注册用户")
                        .param("password", "pass123")
                        .param("email", "reg@test.com")
                        .param("phone", "13800000000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("login"));

        User inserted = userDao.findByUserID(userId);
        assertNotNull(inserted);
        assertEquals("注册用户", inserted.getUserName());
    }

    @Test
    @Tag("P0")
    void itAuth07_logout_shouldClearUserSessionAndRedirectIndex() throws Exception {
        MockHttpSession session = userSession("test");

        MvcResult result = mockMvc.perform(get("/logout.do").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"))
                .andReturn();

        assertNull(result.getRequest().getSession(false).getAttribute("user"));
    }

    @Test
    @Tag("P0")
    void itAuth08_quit_shouldClearAdminSessionAndRedirectIndex() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("admin", userDao.findByUserID("admin"));

        MvcResult result = mockMvc.perform(get("/quit.do").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"))
                .andReturn();

        assertNull(result.getRequest().getSession(false).getAttribute("admin"));
    }

    @Test
    @Tag("P1")
    void itAuth09_checkPasswordCorrect_shouldReturnTrue() throws Exception {
        mockMvc.perform(get("/checkPassword.do")
                        .param("userID", "test")
                        .param("password", "test"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @Tag("P1")
    void itAuth10_checkPasswordWrong_shouldReturnFalse() throws Exception {
        mockMvc.perform(get("/checkPassword.do")
                        .param("userID", "test")
                        .param("password", "wrong"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @Tag("P0")
    void itAuth11_updateUser_shouldRedirectAndUpdateDatabaseAndSession() throws Exception {
        MockHttpSession session = userSession("test");
        MultipartFile emptyPicture = new MockMultipartFile("picture", "", "application/octet-stream", new byte[0]);

        MvcResult result = mockMvc.perform(multipart("/updateUser.do")
                        .file((MockMultipartFile) emptyPicture)
                        .param("userName", "test_updated")
                        .param("userID", "test")
                        .param("passwordNew", "new_password")
                        .param("email", "updated@test.com")
                        .param("phone", "13912345678")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("user_info"))
                .andReturn();

        User updated = userDao.findByUserID("test");
        assertNotNull(updated);
        assertEquals("test_updated", updated.getUserName());
        assertEquals("new_password", updated.getPassword());
        assertEquals("updated@test.com", updated.getEmail());
        assertEquals("13912345678", updated.getPhone());

        Object userInSession = result.getRequest().getSession(false).getAttribute("user");
        assertNotNull(userInSession);
        assertTrue(userInSession instanceof User);
        assertEquals("test_updated", ((User) userInSession).getUserName());
    }

    @Test
    @Tag("P1")
    void itAuth12_userInfo_shouldReturnUserInfoView() throws Exception {
        mockMvc.perform(get("/user_info"))
                .andExpect(status().isOk())
                .andExpect(view().name("user_info"));
    }
}
