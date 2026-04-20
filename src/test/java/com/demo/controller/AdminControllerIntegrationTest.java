package com.demo.controller;

import com.demo.dao.MessageDao;
import com.demo.dao.NewsDao;
import com.demo.dao.OrderDao;
import com.demo.dao.UserDao;
import com.demo.dao.VenueDao;
import com.demo.entity.Message;
import com.demo.entity.News;
import com.demo.entity.Order;
import com.demo.entity.User;
import com.demo.entity.Venue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDao userDao;

    @Autowired
    private VenueDao venueDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private MessageDao messageDao;

    @Autowired
    private NewsDao newsDao;

    @Autowired
    private ObjectMapper objectMapper;

    private String unique(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private User createUser(String userId) {
        User user = new User();
        user.setUserID(userId);
        user.setUserName("name_" + userId);
        user.setPassword("pwd");
        user.setEmail("u@test.com");
        user.setPhone("13800000000");
        user.setIsadmin(0);
        user.setPicture("");
        return userDao.save(user);
    }

    private Venue createVenue(String venueName) {
        Venue venue = new Venue();
        venue.setVenueName(venueName);
        venue.setAddress("上海");
        venue.setDescription("desc");
        venue.setPrice(300);
        venue.setPicture("");
        venue.setOpen_time("09:00");
        venue.setClose_time("20:00");
        return venueDao.save(venue);
    }

    private Order createOrder(int state) {
        Order order = new Order();
        order.setUserID("test");
        order.setVenueID(16);
        order.setState(state);
        order.setOrderTime(LocalDateTime.now());
        order.setStartTime(LocalDateTime.now().plusDays(1));
        order.setHours(2);
        order.setTotal(1000);
        return orderDao.save(order);
    }

    private Message createMessage(int state) {
        Message message = new Message();
        message.setUserID("test");
        message.setContent(unique("msg"));
        message.setTime(LocalDateTime.now());
        message.setState(state);
        return messageDao.save(message);
    }

    private News createNews() {
        News news = new News();
        news.setTitle(unique("news"));
        news.setContent("content");
        news.setTime(LocalDateTime.now());
        return newsDao.save(news);
    }

    @Test
    @Tag("P0")
    void itAdmUser01_userManage_shouldReturnViewAndTotal() throws Exception {
        mockMvc.perform(get("/user_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user_manage"))
                .andExpect(model().attributeExists("total"));
    }

    @Test
    @Tag("P0")
    void itAdmUser02_userList_shouldReturnNormalUsersOnly() throws Exception {
        String json = mockMvc.perform(get("/userList.do").param("page", "1"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode array = objectMapper.readTree(json);
        assertTrue(array.isArray());
        assertTrue(array.size() <= 10);
        Iterator<JsonNode> it = array.elements();
        while (it.hasNext()) {
            JsonNode node = it.next();
            assertEquals(0, node.get("isadmin").asInt());
        }
    }

    @Test
    @Tag("P1")
    void itAdmUser03_checkUserID_shouldReturnFalseForExistingAndTrueForNew() throws Exception {
        mockMvc.perform(post("/checkUserID.do").param("userID", "test"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        mockMvc.perform(post("/checkUserID.do").param("userID", unique("new_user")))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @Tag("P0")
    void itAdmUser04_addUser_shouldRedirectAndInsert() throws Exception {
        String userId = unique("it_add_user");

        mockMvc.perform(post("/addUser.do")
                        .param("userID", userId)
                        .param("userName", "added")
                        .param("password", "pwd")
                        .param("email", "a@test.com")
                        .param("phone", "13800000001"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("user_manage"));

        assertNotNull(userDao.findByUserID(userId));
    }

    @Test
    @Tag("P0")
    void itAdmUser05_modifyUser_shouldRedirectAndUpdate() throws Exception {
        User user = createUser(unique("it_old_user"));
        String newUserId = unique("it_new_user");

        mockMvc.perform(post("/modifyUser.do")
                        .param("oldUserID", user.getUserID())
                        .param("userID", newUserId)
                        .param("userName", "modified")
                        .param("password", "newpwd")
                        .param("email", "m@test.com")
                        .param("phone", "13900000000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("user_manage"));

        User modified = userDao.findByUserID(newUserId);
        assertNotNull(modified);
        assertEquals("modified", modified.getUserName());
    }

    @Test
    @Tag("P0")
    void itAdmUser06_delUser_shouldReturnTrueAndDelete() throws Exception {
        User user = createUser(unique("it_del_user"));

        mockMvc.perform(post("/delUser.do").param("id", String.valueOf(user.getId())))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        assertNull(userDao.findById(user.getId()));
    }

    @Test
    @Tag("P0")
    void itAdmVenue01_venueManage_shouldReturnViewAndTotal() throws Exception {
        mockMvc.perform(get("/venue_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/venue_manage"))
                .andExpect(model().attributeExists("total"));
    }

    @Test
    @Tag("P1")
    void itAdmVenue02_venueList_shouldReturnPagedData() throws Exception {
        String json = mockMvc.perform(get("/venueList.do").param("page", "1"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode array = objectMapper.readTree(json);
        assertTrue(array.isArray());
        assertTrue(array.size() <= 10);
    }

    @Test
    @Tag("P1")
    void itAdmVenue03_checkVenueName_shouldReturnFalseForExistingAndTrueForNew() throws Exception {
        mockMvc.perform(post("/checkVenueName.do").param("venueName", "场馆2"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        mockMvc.perform(post("/checkVenueName.do").param("venueName", unique("venue")))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @Tag("P0")
    void itAdmVenue04_addVenue_shouldRedirectAndInsert() throws Exception {
        String venueName = unique("it_add_venue");

        mockMvc.perform(multipart("/addVenue.do")
                        .file(new MockMultipartFile("picture", "", "application/octet-stream", new byte[0]))
                        .param("venueName", venueName)
                        .param("address", "上海")
                        .param("description", "desc")
                        .param("price", "450")
                        .param("open_time", "09:00")
                        .param("close_time", "20:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("venue_manage"));

        assertNotNull(venueDao.findByVenueName(venueName));
    }

    @Test
    @Tag("P0")
    void itAdmVenue05_modifyVenue_shouldRedirectAndUpdate() throws Exception {
        Venue venue = createVenue(unique("it_old_venue"));

        mockMvc.perform(multipart("/modifyVenue.do")
                        .file(new MockMultipartFile("picture", "", "application/octet-stream", new byte[0]))
                        .param("venueID", String.valueOf(venue.getVenueID()))
                        .param("venueName", venue.getVenueName())
                        .param("address", "上海黄浦")
                        .param("description", "new_desc")
                        .param("price", "888")
                        .param("open_time", "08:00")
                        .param("close_time", "22:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("venue_manage"));

        Venue updated = venueDao.findByVenueID(venue.getVenueID());
        assertNotNull(updated);
        assertEquals(888, updated.getPrice());
        assertEquals("上海黄浦", updated.getAddress());
    }

    @Test
    @Tag("P0")
    void itAdmVenue06_delVenue_shouldReturnTrueAndDelete() throws Exception {
        Venue venue = createVenue(unique("it_del_venue"));

        mockMvc.perform(post("/delVenue.do").param("venueID", String.valueOf(venue.getVenueID())))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        assertNull(venueDao.findByVenueID(venue.getVenueID()));
    }

    @Test
    @Tag("P0")
    void itAdmOrder01_reservationManage_shouldReturnViewAndModels() throws Exception {
        mockMvc.perform(get("/reservation_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/reservation_manage"))
                .andExpect(model().attributeExists("total"))
                .andExpect(model().attributeExists("order_list"));
    }

    @Test
    @Tag("P0")
    void itAdmOrder02_getNoAuditOrderList_shouldReturnStateNoAudit() throws Exception {
        createOrder(1);

        String json = mockMvc.perform(get("/admin/getOrderList.do").param("page", "1"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode array = objectMapper.readTree(json);
        assertTrue(array.isArray());
        Iterator<JsonNode> it = array.elements();
        while (it.hasNext()) {
            assertEquals(1, it.next().get("state").asInt());
        }
    }

    @Test
    @Tag("P0")
    void itAdmOrder03_passOrder_shouldSetStateWait() throws Exception {
        Order order = createOrder(1);

        mockMvc.perform(post("/passOrder.do").param("orderID", String.valueOf(order.getOrderID())))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        assertEquals(2, orderDao.findByOrderID(order.getOrderID()).getState());
    }

    @Test
    @Tag("P0")
    void itAdmOrder04_rejectOrder_shouldSetStateReject() throws Exception {
        Order order = createOrder(1);

        mockMvc.perform(post("/rejectOrder.do").param("orderID", String.valueOf(order.getOrderID())))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        assertEquals(4, orderDao.findByOrderID(order.getOrderID()).getState());
    }

    @Test
    @Tag("P0")
    void itAdmMsg01_messageManage_shouldReturnViewAndTotal() throws Exception {
        mockMvc.perform(get("/message_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/message_manage"))
                .andExpect(model().attributeExists("total"));
    }

    @Test
    @Tag("P0")
    void itAdmMsg02_messageList_shouldReturnNoAuditMessages() throws Exception {
        createMessage(1);

        String json = mockMvc.perform(get("/messageList.do").param("page", "1"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode array = objectMapper.readTree(json);
        assertTrue(array.isArray());
        Iterator<JsonNode> it = array.elements();
        while (it.hasNext()) {
            assertEquals(1, it.next().get("state").asInt());
        }
    }

    @Test
    @Tag("P0")
    void itAdmMsg03_passMessage_shouldSetStatePass() throws Exception {
        Message message = createMessage(1);

        mockMvc.perform(post("/passMessage.do").param("messageID", String.valueOf(message.getMessageID())))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        assertEquals(2, messageDao.findByMessageID(message.getMessageID()).getState());
    }

    @Test
    @Tag("P0")
    void itAdmMsg04_rejectMessage_shouldSetStateReject() throws Exception {
        Message message = createMessage(1);

        mockMvc.perform(post("/rejectMessage.do").param("messageID", String.valueOf(message.getMessageID())))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        assertEquals(3, messageDao.findByMessageID(message.getMessageID()).getState());
    }

    @Test
    @Tag("P1")
    void itAdmMsg05_delMessage_shouldDeleteMessage() throws Exception {
        Message message = createMessage(1);

        mockMvc.perform(post("/delMessage.do").param("messageID", String.valueOf(message.getMessageID())))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        assertNull(messageDao.findByMessageID(message.getMessageID()));
    }

    @Test
    @Tag("P0")
    void itAdmNews01_newsManage_shouldReturnViewAndTotal() throws Exception {
        mockMvc.perform(get("/news_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/news_manage"))
                .andExpect(model().attributeExists("total"));
    }

    @Test
    @Tag("P1")
    void itAdmNews02_newsList_shouldReturnPagedNews() throws Exception {
        String json = mockMvc.perform(get("/newsList.do").param("page", "1"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode array = objectMapper.readTree(json);
        assertTrue(array.isArray());
        assertTrue(array.size() <= 10);
    }

    @Test
    @Tag("P0")
    void itAdmNews03_addNews_shouldRedirectAndInsert() throws Exception {
        String title = unique("it_add_news");

        mockMvc.perform(post("/addNews.do")
                        .param("title", title)
                        .param("content", "content"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("news_manage"));

        boolean exists = newsDao.findAll().stream().anyMatch(n -> title.equals(n.getTitle()));
        assertTrue(exists);
    }

    @Test
    @Tag("P0")
    void itAdmNews04_modifyNews_shouldRedirectAndUpdate() throws Exception {
        News news = createNews();

        mockMvc.perform(post("/modifyNews.do")
                        .param("newsID", String.valueOf(news.getNewsID()))
                        .param("title", "modified_title")
                        .param("content", "modified_content"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("news_manage"));

        News updated = newsDao.getOne(news.getNewsID());
        assertEquals("modified_title", updated.getTitle());
        assertEquals("modified_content", updated.getContent());
    }

    @Test
    @Tag("P0")
    void itAdmNews05_delNews_shouldReturnTrueAndDelete() throws Exception {
        News news = createNews();

        mockMvc.perform(post("/delNews.do").param("newsID", String.valueOf(news.getNewsID())))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        boolean exists = newsDao.findAll().stream().anyMatch(n -> n.getNewsID() == news.getNewsID());
        assertTrue(!exists);
    }
}
