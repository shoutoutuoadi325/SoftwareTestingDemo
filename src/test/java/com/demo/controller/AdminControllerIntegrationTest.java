package com.demo.controller;

import com.demo.controller.admin.AdminMessageController;
import com.demo.controller.admin.AdminNewsController;
import com.demo.controller.admin.AdminOrderController;
import com.demo.controller.admin.AdminUserController;
import com.demo.controller.admin.AdminVenueController;
import com.demo.entity.Message;
import com.demo.entity.News;
import com.demo.entity.Order;
import com.demo.entity.User;
import com.demo.entity.Venue;
import com.demo.entity.vo.MessageVo;
import com.demo.entity.vo.OrderVo;
import com.demo.service.MessageService;
import com.demo.service.MessageVoService;
import com.demo.service.NewsService;
import com.demo.service.OrderService;
import com.demo.service.OrderVoService;
import com.demo.service.UserService;
import com.demo.service.VenueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class AdminControllerIntegrationTest {

    @Mock
    private UserService userService;

    @Mock
    private VenueService venueService;

    @Mock
    private OrderService orderService;

    @Mock
    private OrderVoService orderVoService;

    @Mock
    private NewsService newsService;

    @Mock
    private MessageService messageService;

    @Mock
    private MessageVoService messageVoService;

    @InjectMocks
    private AdminUserController adminUserController;

    @InjectMocks
    private AdminVenueController adminVenueController;

    @InjectMocks
    private AdminOrderController adminOrderController;

    @InjectMocks
    private AdminNewsController adminNewsController;

    @InjectMocks
    private AdminMessageController adminMessageController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                adminUserController,
                adminVenueController,
                adminOrderController,
                adminNewsController,
                adminMessageController
        ).setViewResolvers(viewResolver()).build();
    }

    private InternalResourceViewResolver viewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/templates/");
        resolver.setSuffix(".html");
        return resolver;
    }

    @Test
    void itAdmUser01_userManage_shouldReturnViewAndTotal() throws Exception {
        Page<User> page = new PageImpl<>(Collections.singletonList(buildUser(1, "u1001")), PageRequest.of(0, 10), 16);
        when(userService.findByUserID(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/user_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user_manage"))
                .andExpect(model().attribute("total", 2));
    }

    @Test
    void itAdmUser02_userAdd_shouldReturnAddView() throws Exception {
        mockMvc.perform(get("/user_add"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user_add"));
    }

    @Test
    void itAdmUser03_userList_shouldReturnUsers() throws Exception {
        Page<User> page = new PageImpl<>(Arrays.asList(buildUser(1, "u1"), buildUser(2, "u2")), PageRequest.of(0, 10), 2);
        when(userService.findByUserID(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/userList.do").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"id\":1},{\"id\":2}]", false));
    }

    @Test
    void itAdmUser04_userEdit_shouldReturnEditViewAndUser() throws Exception {
        when(userService.findById(3)).thenReturn(buildUser(3, "u3"));

        mockMvc.perform(get("/user_edit").param("id", "3"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user_edit"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void itAdmUser05_modifyUser_shouldRedirectAndUpdate() throws Exception {
        User target = buildUser(5, "oldU");
        when(userService.findByUserID("oldU")).thenReturn(target);

        mockMvc.perform(post("/modifyUser.do")
                        .param("userID", "newU")
                        .param("oldUserID", "oldU")
                        .param("userName", "newName")
                        .param("password", "newPass")
                        .param("email", "new@mail.com")
                        .param("phone", "13800000000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("user_manage"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userService).updateUser(captor.capture());
        User updated = captor.getValue();
        assertEquals("newU", updated.getUserID());
        assertEquals("newName", updated.getUserName());
    }

    @Test
    void itAdmUser06_addUser_shouldRedirectAndInsert() throws Exception {
        mockMvc.perform(post("/addUser.do")
                        .param("userID", "u6001")
                        .param("userName", "name")
                        .param("password", "pass")
                        .param("email", "u6001@mail.com")
                        .param("phone", "13000000000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("user_manage"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userService).create(captor.capture());
        assertEquals("u6001", captor.getValue().getUserID());
    }

    @Test
    void itAdmUser07_checkUserID_existingShouldReturnFalse() throws Exception {
        when(userService.countUserID("u1001")).thenReturn(1);

        mockMvc.perform(post("/checkUserID.do").param("userID", "u1001"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void itAdmUser08_checkUserID_newShouldReturnTrue() throws Exception {
        when(userService.countUserID("u9001")).thenReturn(0);

        mockMvc.perform(post("/checkUserID.do").param("userID", "u9001"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void itAdmUser09_delUser_shouldReturnTrueAndDelete() throws Exception {
        mockMvc.perform(post("/delUser.do").param("id", "9"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(userService).delByID(9);
    }

    @Test
    void itAdmUser10_modifyUserWhenOldIdMissing_shouldReturnNotFound() throws Exception {
        when(userService.findByUserID("missing")).thenReturn(null);

        mockMvc.perform(post("/modifyUser.do")
                        .param("userID", "newU")
                        .param("oldUserID", "missing")
                        .param("userName", "newName")
                        .param("password", "newPass")
                        .param("email", "new@mail.com")
                        .param("phone", "13800000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    void itAdmVenue01_venueManage_shouldReturnViewAndTotal() throws Exception {
        Page<Venue> page = new PageImpl<>(Collections.singletonList(buildVenue(1, "v1")), PageRequest.of(0, 10), 21);
        when(venueService.findAll(any())).thenReturn(page);

        mockMvc.perform(get("/venue_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/venue_manage"))
                .andExpect(model().attribute("total", 3));
    }

    @Test
    void itAdmVenue02_venueEdit_shouldReturnViewAndVenue() throws Exception {
        when(venueService.findByVenueID(2)).thenReturn(buildVenue(2, "v2"));

        mockMvc.perform(get("/venue_edit").param("venueID", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("/admin/venue_edit"))
                .andExpect(model().attributeExists("venue"));
    }

    @Test
    void itAdmVenue03_venueAdd_shouldReturnView() throws Exception {
        mockMvc.perform(get("/venue_add"))
                .andExpect(status().isOk())
                .andExpect(view().name("/admin/venue_add"));
    }

    @Test
    void itAdmVenue04_venueList_shouldReturnPagedData() throws Exception {
        Page<Venue> page = new PageImpl<>(Arrays.asList(buildVenue(3, "v3"), buildVenue(4, "v4")));
        when(venueService.findAll(any())).thenReturn(page);

        mockMvc.perform(get("/venueList.do").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"venueID\":3},{\"venueID\":4}]", false));
    }

    @Test
    void itAdmVenue05_addVenue_shouldRedirectAndInsert() throws Exception {
        when(venueService.create(any())).thenReturn(10);
        MockMultipartFile picture = new MockMultipartFile("picture", "", "application/octet-stream", new byte[0]);

        mockMvc.perform(multipart("/addVenue.do")
                        .file(picture)
                        .param("venueName", "newVenue")
                        .param("address", "addr")
                        .param("description", "desc")
                        .param("price", "120")
                        .param("open_time", "08:00")
                        .param("close_time", "22:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("venue_manage"));

        verify(venueService).create(any(Venue.class));
    }

    @Test
    void itAdmVenue06_modifyVenue_shouldRedirectAndUpdate() throws Exception {
        Venue venue = buildVenue(11, "oldVenue");
        when(venueService.findByVenueID(11)).thenReturn(venue);
        MockMultipartFile picture = new MockMultipartFile("picture", "", "application/octet-stream", new byte[0]);

        mockMvc.perform(multipart("/modifyVenue.do")
                        .file(picture)
                        .param("venueID", "11")
                        .param("venueName", "newVenue")
                        .param("address", "addr2")
                        .param("description", "desc2")
                        .param("price", "150")
                        .param("open_time", "09:00")
                        .param("close_time", "21:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("venue_manage"));

        verify(venueService).update(venue);
    }

    @Test
    void itAdmVenue07_delVenue_shouldReturnTrueAndDelete() throws Exception {
        mockMvc.perform(post("/delVenue.do").param("venueID", "12"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(venueService).delById(12);
    }

    @Test
    void itAdmVenue08_checkVenueName_existingShouldReturnFalse() throws Exception {
        when(venueService.countVenueName("v-exists")).thenReturn(1);

        mockMvc.perform(post("/checkVenueName.do").param("venueName", "v-exists"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void itAdmVenue09_checkVenueName_newShouldReturnTrue() throws Exception {
        when(venueService.countVenueName("v-new")).thenReturn(0);

        mockMvc.perform(post("/checkVenueName.do").param("venueName", "v-new"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void itAdmOrder01_reservationManage_shouldReturnViewAndModels() throws Exception {
        when(orderService.findAuditOrder()).thenReturn(Collections.singletonList(buildOrder(1, "u1001", 1, OrderService.STATE_WAIT)));
        when(orderVoService.returnVo(any())).thenReturn(Collections.singletonList(buildOrderVo(1, "u1001")));
        when(orderService.findNoAuditOrder(any())).thenReturn(new PageImpl<>(Collections.singletonList(buildOrder(2, "u1002", 1, OrderService.STATE_NO_AUDIT)), PageRequest.of(0, 10), 11));

        mockMvc.perform(get("/reservation_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/reservation_manage"))
                .andExpect(model().attributeExists("order_list"))
                .andExpect(model().attributeExists("total"));
    }

    @Test
    void itAdmOrder02_getNoAuditOrderList_shouldReturnStateNoAudit() throws Exception {
        when(orderService.findNoAuditOrder(any())).thenReturn(new PageImpl<>(Collections.singletonList(buildOrder(3, "u1003", 1, OrderService.STATE_NO_AUDIT))));
        when(orderVoService.returnVo(any())).thenReturn(Collections.singletonList(buildOrderVo(3, "u1003")));

        mockMvc.perform(get("/admin/getOrderList.do").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"orderID\":3}]", false));
    }

    @Test
    void itAdmOrder03_passOrder_shouldSetStateWait() throws Exception {
        mockMvc.perform(post("/passOrder.do").param("orderID", "13"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(orderService).confirmOrder(13);
    }

    @Test
    void itAdmOrder04_rejectOrder_shouldSetStateReject() throws Exception {
        mockMvc.perform(post("/rejectOrder.do").param("orderID", "14"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(orderService).rejectOrder(14);
    }

    @Test
    void itAdmNews01_newsManage_shouldReturnViewAndTotal() throws Exception {
        when(newsService.findAll(any())).thenReturn(new PageImpl<>(Collections.singletonList(buildNews(1, "n1")), PageRequest.of(0, 10), 12));

        mockMvc.perform(get("/news_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/news_manage"))
                .andExpect(model().attribute("total", 2));
    }

    @Test
    void itAdmNews02_newsAdd_shouldReturnAddView() throws Exception {
        mockMvc.perform(get("/news_add"))
                .andExpect(status().isOk())
                .andExpect(view().name("/admin/news_add"));
    }

    @Test
    void itAdmNews03_newsEdit_shouldReturnEditView() throws Exception {
        when(newsService.findById(4)).thenReturn(buildNews(4, "n4"));

        mockMvc.perform(get("/news_edit").param("newsID", "4"))
                .andExpect(status().isOk())
                .andExpect(view().name("/admin/news_edit"))
                .andExpect(model().attributeExists("news"));
    }

    @Test
    void itAdmNews04_newsList_shouldReturnPagedNews() throws Exception {
        when(newsService.findAll(any())).thenReturn(new PageImpl<>(Arrays.asList(buildNews(6, "n6"), buildNews(7, "n7"))));

        mockMvc.perform(get("/newsList.do").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"newsID\":6},{\"newsID\":7}]", false));
    }

    @Test
    void itAdmNews05_delNews_shouldReturnTrueAndDelete() throws Exception {
        mockMvc.perform(post("/delNews.do").param("newsID", "8"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(newsService).delById(8);
    }

    @Test
    void itAdmNews06_modifyNews_shouldRedirectAndUpdate() throws Exception {
        News news = buildNews(9, "old");
        when(newsService.findById(9)).thenReturn(news);

        mockMvc.perform(post("/modifyNews.do")
                        .param("newsID", "9")
                        .param("title", "newTitle")
                        .param("content", "newContent"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("news_manage"));

        verify(newsService).update(news);
        assertEquals("newTitle", news.getTitle());
        assertEquals("newContent", news.getContent());
    }

    @Test
    void itAdmNews07_addNews_shouldRedirectAndInsert() throws Exception {
        mockMvc.perform(post("/addNews.do")
                        .param("title", "t")
                        .param("content", "c"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("news_manage"));

        verify(newsService).create(any(News.class));
    }

    @Test
    void itAdmNews08_modifyNewsWhenTargetMissing_shouldReturnNotFound() throws Exception {
        when(newsService.findById(404)).thenReturn(null);

        mockMvc.perform(post("/modifyNews.do")
                        .param("newsID", "404")
                        .param("title", "newTitle")
                        .param("content", "newContent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void itAdmMsg01_messageManage_shouldReturnViewAndTotal() throws Exception {
        when(messageService.findWaitState(any())).thenReturn(new PageImpl<>(Collections.singletonList(buildMessage(1, "u1001", MessageService.STATE_NO_AUDIT)), PageRequest.of(0, 10), 17));

        mockMvc.perform(get("/message_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/message_manage"))
                .andExpect(model().attribute("total", 2));
    }

    @Test
    void itAdmMsg02_messageList_shouldReturnNoAuditMessages() throws Exception {
        when(messageService.findWaitState(any())).thenReturn(new PageImpl<>(Collections.singletonList(buildMessage(2, "u1001", MessageService.STATE_NO_AUDIT))));
        when(messageVoService.returnVo(any())).thenReturn(Collections.singletonList(buildMessageVo(2, "u1001")));

        mockMvc.perform(get("/messageList.do").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"messageID\":2}]", false));
    }

    @Test
    void itAdmMsg03_passMessage_shouldSetStatePass() throws Exception {
        mockMvc.perform(post("/passMessage.do").param("messageID", "3"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(messageService).confirmMessage(3);
    }

    @Test
    void itAdmMsg04_rejectMessage_shouldSetStateReject() throws Exception {
        mockMvc.perform(post("/rejectMessage.do").param("messageID", "4"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(messageService).rejectMessage(4);
    }

    @Test
    void itAdmMsg05_delMessage_shouldDeleteMessage() throws Exception {
        mockMvc.perform(post("/delMessage.do").param("messageID", "5"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(messageService).delById(5);
    }

    private User buildUser(int id, String userID) {
        User user = new User();
        user.setId(id);
        user.setUserID(userID);
        user.setUserName("name-" + userID);
        user.setPassword("123456");
        user.setEmail(userID + "@mail.com");
        user.setPhone("13000000000");
        user.setPicture("");
        return user;
    }

    private Venue buildVenue(int id, String name) {
        Venue venue = new Venue();
        venue.setVenueID(id);
        venue.setVenueName(name);
        venue.setDescription("desc");
        venue.setPrice(100);
        venue.setPicture("");
        venue.setAddress("addr");
        venue.setOpen_time("08:00");
        venue.setClose_time("22:00");
        return venue;
    }

    private Order buildOrder(int id, String userID, int venueID, int state) {
        Order order = new Order();
        order.setOrderID(id);
        order.setUserID(userID);
        order.setVenueID(venueID);
        order.setState(state);
        order.setHours(2);
        order.setTotal(200);
        order.setOrderTime(LocalDateTime.of(2026, 4, 23, 10, 0, 0));
        order.setStartTime(LocalDateTime.of(2026, 4, 24, 9, 0, 0));
        return order;
    }

    private OrderVo buildOrderVo(int id, String userID) {
        OrderVo vo = new OrderVo();
        vo.setOrderID(id);
        vo.setUserID(userID);
        vo.setVenueID(1);
        vo.setVenueName("venue");
        vo.setState(OrderService.STATE_NO_AUDIT);
        vo.setHours(2);
        vo.setTotal(200);
        vo.setOrderTime(LocalDateTime.of(2026, 4, 23, 10, 0, 0));
        vo.setStartTime(LocalDateTime.of(2026, 4, 24, 9, 0, 0));
        return vo;
    }

    private News buildNews(int id, String title) {
        News news = new News();
        news.setNewsID(id);
        news.setTitle(title);
        news.setContent("content");
        news.setTime(LocalDateTime.of(2026, 4, 23, 10, 0, 0));
        return news;
    }

    private Message buildMessage(int id, String userID, int state) {
        Message message = new Message();
        message.setMessageID(id);
        message.setUserID(userID);
        message.setContent("content");
        message.setState(state);
        message.setTime(LocalDateTime.of(2026, 4, 23, 10, 0, 0));
        return message;
    }

    private MessageVo buildMessageVo(int id, String userID) {
        MessageVo vo = new MessageVo();
        vo.setMessageID(id);
        vo.setUserID(userID);
        vo.setContent("content");
        vo.setState(MessageService.STATE_NO_AUDIT);
        vo.setUserName("name");
        vo.setPicture("pic.png");
        vo.setTime(LocalDateTime.of(2026, 4, 23, 10, 0, 0));
        return vo;
    }
}
