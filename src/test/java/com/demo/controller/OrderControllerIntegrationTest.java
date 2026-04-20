package com.demo.controller;

import com.demo.dao.OrderDao;
import com.demo.dao.UserDao;
import com.demo.dao.VenueDao;
import com.demo.entity.Order;
import com.demo.entity.User;
import com.demo.entity.Venue;
import com.demo.exception.LoginException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private VenueDao venueDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ObjectMapper objectMapper;

    private MockHttpSession userSession() {
        MockHttpSession session = new MockHttpSession();
        User user = userDao.findByUserID("test");
        session.setAttribute("user", user);
        return session;
    }

    private Order createOrder(String userId, int venueId, int state) {
        Order order = new Order();
        order.setUserID(userId);
        order.setVenueID(venueId);
        order.setState(state);
        order.setOrderTime(LocalDateTime.now());
        order.setStartTime(LocalDateTime.now().plusDays(2));
        order.setHours(2);
        order.setTotal(1000);
        return orderDao.save(order);
    }

    @Test
    @Tag("P0")
    void itOrd01_orderManageWithSession_shouldReturnViewAndTotal() throws Exception {
        mockMvc.perform(get("/order_manage").session(userSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("order_manage"))
                .andExpect(model().attributeExists("total"));
    }

    @Test
    @Tag("P0")
    void itOrd02_orderManageWithoutSession_shouldThrowLoginException() throws Exception {
        mockMvc.perform(get("/order_manage"))
                .andExpect(status().is5xxServerError())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof LoginException));
    }

    @Test
    @Tag("P0")
    void itOrd03_orderPlaceByVenueId_shouldReturnOrderPlaceView() throws Exception {
        mockMvc.perform(get("/order_place.do").param("venueID", "16"))
                .andExpect(status().isOk())
                .andExpect(view().name("order_place"))
                .andExpect(model().attributeExists("venue"));
    }

    @Test
    @Tag("P0")
    void itOrd04_getOrderListWithSession_shouldReturnOrdersOfUser() throws Exception {
        String json = mockMvc.perform(get("/getOrderList.do")
                        .param("page", "1")
                        .session(userSession()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode array = objectMapper.readTree(json);
        assertTrue(array.isArray());
        Iterator<JsonNode> it = array.elements();
        while (it.hasNext()) {
            JsonNode node = it.next();
            assertEquals("test", node.get("userID").asText());
        }
    }

    @Test
    @Tag("P0")
    void itOrd05_getOrderListWithoutSession_shouldThrowLoginException() throws Exception {
        mockMvc.perform(get("/getOrderList.do").param("page", "1"))
                .andExpect(status().is5xxServerError())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof LoginException));
    }

    @Test
    @Tag("P0")
    void itOrd06_addOrderWithSession_shouldInsertNoAuditOrderAndRedirect() throws Exception {
        long before = orderDao.count();
        LocalDateTime expectedStart = LocalDateTime.parse("2026-05-01 10:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        mockMvc.perform(post("/addOrder.do")
                        .session(userSession())
                        .param("venueName", "场馆2")
                        .param("date", "ignored")
                        .param("startTime", "2026-05-01 10:00")
                        .param("hours", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("order_manage"));

        long after = orderDao.count();
        assertEquals(before + 1, after);

        Venue venue = venueDao.findByVenueName("场馆2");
        boolean created = orderDao.findAll().stream().anyMatch(order ->
                "test".equals(order.getUserID())
                        && order.getVenueID() == venue.getVenueID()
                        && order.getState() == 1
                        && expectedStart.equals(order.getStartTime())
                        && order.getTotal() == 1000);
        assertTrue(created);
    }

    @Test
    @Tag("P0")
    void itOrd07_addOrderWithoutSession_shouldThrowLoginException() throws Exception {
        mockMvc.perform(post("/addOrder.do")
                        .param("venueName", "场馆2")
                        .param("date", "ignored")
                        .param("startTime", "2026-05-01 10:00")
                        .param("hours", "2"))
                .andExpect(status().is5xxServerError())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof LoginException));
    }

    @Test
    @Tag("P0")
    void itOrd08_finishOrder_shouldUpdateStateToFinish() throws Exception {
        Order order = createOrder("test", 16, 2);

        mockMvc.perform(post("/finishOrder.do")
                        .param("orderID", String.valueOf(order.getOrderID())))
                .andExpect(status().isOk());

        Order updated = orderDao.findByOrderID(order.getOrderID());
        assertNotNull(updated);
        assertEquals(3, updated.getState());
    }

    @Test
    @Tag("P1")
    void itOrd09_modifyOrderPage_shouldReturnOrderEditView() throws Exception {
        Order order = createOrder("test", 16, 1);

        mockMvc.perform(get("/modifyOrder.do")
                        .param("orderID", String.valueOf(order.getOrderID())))
                .andExpect(status().isOk())
                .andExpect(view().name("order_edit"))
                .andExpect(model().attributeExists("order"))
                .andExpect(model().attributeExists("venue"));
    }

    @Test
    @Tag("P0")
    void itOrd10_modifyOrderWithSession_shouldResetToNoAuditAndRedirect() throws Exception {
        Order order = createOrder("test", 16, 2);

        mockMvc.perform(post("/modifyOrder")
                        .session(userSession())
                        .param("orderID", String.valueOf(order.getOrderID()))
                        .param("venueName", "场馆3")
                        .param("date", "ignored")
                        .param("startTime", "2026-06-02 09:00")
                        .param("hours", "4"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("order_manage"));

        Order updated = orderDao.findByOrderID(order.getOrderID());
        assertNotNull(updated);
        assertEquals(1, updated.getState());
        assertEquals(4, updated.getHours());
        assertEquals(17, updated.getVenueID());
        assertEquals(1200, updated.getTotal());
    }

    @Test
    @Tag("P0")
    void itOrd11_deleteOrder_shouldReturnTrueAndDeleteRecord() throws Exception {
        Order order = createOrder("test", 16, 1);

        mockMvc.perform(post("/delOrder.do")
                        .param("orderID", String.valueOf(order.getOrderID())))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        assertEquals(null, orderDao.findByOrderID(order.getOrderID()));
    }

    @Test
    @Tag("P1")
    void itOrd12_getVenueOrderByDate_shouldReturnVenueAndOrders() throws Exception {
        mockMvc.perform(get("/order/getOrderList.do")
                        .param("venueName", "场馆2")
                        .param("date", "2020-01-24"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(result -> {
                    JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
                    assertNotNull(root.get("venue"));
                    assertNotNull(root.get("orders"));
                    assertTrue(root.get("orders").isArray());
                });
    }

    @Test
    @Tag("P2")
    void itOrd13_modifyOrderWithInvalidTime_shouldReturn5xx() throws Exception {
        Order order = createOrder("test", 16, 1);

        mockMvc.perform(post("/modifyOrder")
                        .session(userSession())
                        .param("orderID", String.valueOf(order.getOrderID()))
                        .param("venueName", "场馆2")
                        .param("date", "ignored")
                        .param("startTime", "bad")
                        .param("hours", "2"))
                .andExpect(status().is5xxServerError());
    }
}
