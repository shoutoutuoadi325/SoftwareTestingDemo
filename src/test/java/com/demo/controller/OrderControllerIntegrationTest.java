package com.demo.controller;

import com.demo.controller.user.OrderController;
import com.demo.entity.Order;
import com.demo.entity.User;
import com.demo.entity.Venue;
import com.demo.entity.vo.OrderVo;
import com.demo.service.OrderService;
import com.demo.service.OrderVoService;
import com.demo.service.VenueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class OrderControllerIntegrationTest {

    @Mock
    private OrderService orderService;

    @Mock
    private OrderVoService orderVoService;

    @Mock
    private VenueService venueService;

    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
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
    void itOrd01_orderManageWithSession_shouldReturnViewAndTotal() throws Exception {
        Page<Order> page = new PageImpl<>(Collections.singletonList(buildOrder(1, "u1001", 1, OrderService.STATE_NO_AUDIT)), PageRequest.of(0, 5), 12);
        when(orderService.findUserOrder(eq("u1001"), any())).thenReturn(page);

        mockMvc.perform(get("/order_manage").sessionAttr("user", buildUser("u1001")))
                .andExpect(status().isOk())
                .andExpect(view().name("order_manage"))
                .andExpect(model().attributeExists("total"));
    }

    @Test
    void itOrd02_orderManageWithoutSession_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/order_manage"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void itOrd03_orderPlaceByVenueId_shouldReturnOrderPlaceView() throws Exception {
        when(venueService.findByVenueID(3)).thenReturn(buildVenue(3, "venue-3"));

        mockMvc.perform(get("/order_place.do").param("venueID", "3"))
                .andExpect(status().isOk())
                .andExpect(view().name("order_place"))
                .andExpect(model().attributeExists("venue"));
    }

    @Test
    void itOrd04_orderPlaceWithoutVenue_shouldReturnOrderPlaceView() throws Exception {
        mockMvc.perform(get("/order_place"))
                .andExpect(status().isOk())
                .andExpect(view().name("order_place"));
    }

    @Test
    void itOrd05_getOrderListWithSession_shouldReturnOrdersOfUser() throws Exception {
        Page<Order> page = new PageImpl<>(Collections.singletonList(buildOrder(2, "u1001", 1, OrderService.STATE_WAIT)));
        when(orderService.findUserOrder(eq("u1001"), any())).thenReturn(page);
        when(orderVoService.returnVo(any())).thenReturn(Collections.singletonList(buildOrderVo(2, "u1001")));

        mockMvc.perform(get("/getOrderList.do")
                        .param("page", "1")
                        .sessionAttr("user", buildUser("u1001")))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"orderID\":2,\"userID\":\"u1001\"}]", false));
    }

    @Test
    void itOrd06_getOrderListWithoutSession_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/getOrderList.do").param("page", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void itOrd07_addOrderWithSession_shouldInsertNoAuditOrderAndRedirect() throws Exception {
        mockMvc.perform(post("/addOrder.do")
                        .param("venueName", "gymA")
                        .param("date", "2026-04-26")
                        .param("startTime", "09:30:00")
                        .param("hours", "2")
                        .sessionAttr("user", buildUser("u1001")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("order_manage"));

        verify(orderService).submit("gymA", LocalDateTime.of(2026, 4, 26, 9, 30, 0), 2, "u1001");
    }

    @Test
    void itOrd08_addOrderWithoutSession_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(post("/addOrder.do")
                        .param("venueName", "gymA")
                        .param("date", "2026-04-26")
                        .param("startTime", "09:30:00")
                        .param("hours", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void itOrd09_finishOrder_shouldUpdateStateToFinish() throws Exception {
        mockMvc.perform(post("/finishOrder.do").param("orderID", "8"))
                .andExpect(status().isOk());

        verify(orderService).finishOrder(8);
    }

    @Test
    void itOrd10_modifyOrderPage_shouldReturnOrderEditView() throws Exception {
        Order order = buildOrder(9, "u1001", 4, OrderService.STATE_WAIT);
        Venue venue = buildVenue(4, "venue-4");
        when(orderService.findById(9)).thenReturn(order);
        when(venueService.findByVenueID(4)).thenReturn(venue);

        mockMvc.perform(get("/modifyOrder.do").param("orderID", "9"))
                .andExpect(status().isOk())
                .andExpect(view().name("order_edit"))
                .andExpect(model().attributeExists("order"))
                .andExpect(model().attributeExists("venue"));
    }

    @Test
    void itOrd11_modifyOrderWithSession_shouldResetToNoAuditAndRedirect() throws Exception {
        mockMvc.perform(post("/modifyOrder")
                        .param("venueName", "gymB")
                        .param("date", "2026-04-27")
                        .param("startTime", "10:00:00")
                        .param("hours", "3")
                        .param("orderID", "9")
                        .sessionAttr("user", buildUser("u1001")))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(orderService).updateOrder(9, "gymB", LocalDateTime.of(2026, 4, 27, 10, 0, 0), 3, "u1001");
    }

    @Test
    void itOrd12_modifyOrderWithoutSession_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(post("/modifyOrder")
                        .param("venueName", "gymB")
                        .param("date", "2026-04-27")
                        .param("startTime", "10:00:00")
                        .param("hours", "3")
                        .param("orderID", "9"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void itOrd13_deleteOrder_shouldReturnTrueAndDeleteRecord() throws Exception {
        mockMvc.perform(post("/delOrder.do").param("orderID", "11"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(orderService).delOrder(11);
    }

    @Test
    void itOrd14_getVenueOrderByDate_shouldReturnVenueAndOrders() throws Exception {
        Venue venue = buildVenue(6, "venue-6");
        when(venueService.findByVenueName("venue-6")).thenReturn(venue);
        when(orderService.findDateOrder(eq(6), any(), any())).thenReturn(Arrays.asList(
                buildOrder(31, "u1001", 6, OrderService.STATE_WAIT),
                buildOrder(32, "u1002", 6, OrderService.STATE_NO_AUDIT)
        ));

        mockMvc.perform(get("/order/getOrderList.do")
                        .param("venueName", "venue-6")
                        .param("date", "2026-04-28"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"venue\":{\"venueID\":6},\"orders\":[{\"orderID\":31},{\"orderID\":32}]}", false));
    }

    @Test
    void itOrd15_modifyOrderWithInvalidTime_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/modifyOrder")
                        .param("venueName", "gymB")
                        .param("date", "2026-04-27")
                        .param("startTime", "bad")
                        .param("hours", "3")
                        .param("orderID", "9")
                        .sessionAttr("user", buildUser("u1001")))
                .andExpect(status().isBadRequest());
    }

    private User buildUser(String userID) {
        User user = new User();
        user.setUserID(userID);
        user.setUserName("name");
        return user;
    }

    private Venue buildVenue(int id, String name) {
        Venue venue = new Venue();
        venue.setVenueID(id);
        venue.setVenueName(name);
        venue.setPrice(100);
        venue.setAddress("addr");
        venue.setDescription("desc");
        venue.setPicture("");
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

    private OrderVo buildOrderVo(int orderID, String userID) {
        OrderVo vo = new OrderVo();
        vo.setOrderID(orderID);
        vo.setUserID(userID);
        vo.setVenueID(1);
        vo.setVenueName("venue");
        vo.setState(OrderService.STATE_WAIT);
        vo.setHours(2);
        vo.setTotal(200);
        vo.setOrderTime(LocalDateTime.of(2026, 4, 23, 10, 0, 0));
        vo.setStartTime(LocalDateTime.of(2026, 4, 24, 9, 0, 0));
        return vo;
    }
}
