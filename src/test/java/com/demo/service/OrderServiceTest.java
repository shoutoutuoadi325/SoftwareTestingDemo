package com.demo.service;

import com.demo.dao.OrderDao;
import com.demo.dao.VenueDao;
import com.demo.entity.Order;
import com.demo.entity.Venue;
import com.demo.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderDao orderDao;

    @Mock
    private VenueDao venueDao;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void utOs01_findById_shouldReturnOrder() {
        Order order = buildOrder(1, "u1001", 1, OrderService.STATE_NO_AUDIT, 2, 160);
        when(orderDao.getOne(1)).thenReturn(order);

        Order result = orderService.findById(1);

        assertEquals(1, result.getOrderID());
        verify(orderDao).getOne(1);
    }

    @Test
    void utOs02_findDateOrder_shouldReturnOrdersInRange() {
        LocalDateTime from = LocalDateTime.of(2026, 4, 23, 0, 0, 0);
        LocalDateTime to = from.plusDays(1);
        List<Order> list = Collections.singletonList(buildOrder(2, "u1001", 1, OrderService.STATE_WAIT, 2, 200));
        when(orderDao.findByVenueIDAndStartTimeIsBetween(1, from, to)).thenReturn(list);

        List<Order> result = orderService.findDateOrder(1, from, to);

        assertEquals(1, result.size());
        verify(orderDao).findByVenueIDAndStartTimeIsBetween(1, from, to);
    }

    @Test
    void utOs03_findUserOrder_shouldReturnOrdersByUser() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Order> page = new PageImpl<>(Collections.singletonList(buildOrder(3, "u1001", 1, OrderService.STATE_WAIT, 2, 200)));
        when(orderDao.findAllByUserID("u1001", pageable)).thenReturn(page);

        Page<Order> result = orderService.findUserOrder("u1001", pageable);

        assertEquals(1, result.getTotalElements());
        verify(orderDao).findAllByUserID("u1001", pageable);
    }

    @Test
    void utOs04_submit_shouldCreateNoAuditOrderWithCorrectTotal() {
        Venue venue = buildVenue(7, "羽毛球馆", 60);
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 24, 9, 0, 0);
        when(venueDao.findByVenueName("羽毛球馆")).thenReturn(venue);

        orderService.submit("羽毛球馆", startTime, 3, "u1001");

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderDao).save(captor.capture());
        Order saved = captor.getValue();
        assertEquals(OrderService.STATE_NO_AUDIT, saved.getState());
        assertEquals(7, saved.getVenueID());
        assertEquals(3, saved.getHours());
        assertEquals(180, saved.getTotal());
        assertEquals(startTime, saved.getStartTime());
        assertEquals("u1001", saved.getUserID());
        assertNotNull(saved.getOrderTime());
    }

    @Test
    void utOs05_submit_venueNotFound_shouldThrowIllegalArgumentException() {
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 24, 9, 0, 0);
        when(venueDao.findByVenueName("不存在场馆")).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> orderService.submit("不存在场馆", startTime, 2, "u1001"));
    }

    @Test
    void utOs06_updateOrder_shouldResetStateAndRecalculateTotal() {
        Venue venue = buildVenue(8, "网球馆", 90);
        Order order = buildOrder(11, "u1001", 1, OrderService.STATE_WAIT, 2, 100);
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 25, 10, 0, 0);
        when(venueDao.findByVenueName("网球馆")).thenReturn(venue);
        when(orderDao.findByOrderID(11)).thenReturn(order);

        orderService.updateOrder(11, "网球馆", startTime, 2, "u1002");

        verify(orderDao).save(order);
        assertEquals(OrderService.STATE_NO_AUDIT, order.getState());
        assertEquals(8, order.getVenueID());
        assertEquals(2, order.getHours());
        assertEquals(180, order.getTotal());
        assertEquals(startTime, order.getStartTime());
        assertEquals("u1002", order.getUserID());
        assertNotNull(order.getOrderTime());
    }

    @Test
    void utOs07_updateOrder_orderNotFound_shouldThrowIllegalArgumentException() {
        Venue venue = buildVenue(8, "网球馆", 90);
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 25, 10, 0, 0);
        when(venueDao.findByVenueName("网球馆")).thenReturn(venue);
        when(orderDao.findByOrderID(404)).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> orderService.updateOrder(404, "网球馆", startTime, 2, "u1002"));
    }

    @Test
    void utOs08_updateOrder_venueNotFound_shouldThrowIllegalArgumentException() {
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 25, 10, 0, 0);
        when(venueDao.findByVenueName("bad")).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> orderService.updateOrder(11, "bad", startTime, 2, "u1002"));
    }

    @Test
    void utOs09_delOrder_shouldDeleteById() {
        orderService.delOrder(9);

        verify(orderDao).deleteById(9);
    }

    @Test
    void utOs10_confirmOrder_shouldUpdateStateToWait() {
        Order order = buildOrder(12, "u1001", 1, OrderService.STATE_NO_AUDIT, 2, 120);
        when(orderDao.findByOrderID(12)).thenReturn(order);

        orderService.confirmOrder(12);

        verify(orderDao).updateState(OrderService.STATE_WAIT, 12);
    }

    @Test
    void utOs11_confirmOrder_notFound_shouldThrowRuntimeException() {
        when(orderDao.findByOrderID(999)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> orderService.confirmOrder(999));

        assertEquals("订单不存在", ex.getMessage());
    }

    @Test
    void utOs12_finishOrder_shouldUpdateStateToFinish() {
        Order order = buildOrder(13, "u1001", 1, OrderService.STATE_WAIT, 2, 120);
        when(orderDao.findByOrderID(13)).thenReturn(order);

        orderService.finishOrder(13);

        verify(orderDao).updateState(OrderService.STATE_FINISH, 13);
    }

    @Test
    void utOs13_finishOrder_notFound_shouldThrowRuntimeException() {
        when(orderDao.findByOrderID(997)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> orderService.finishOrder(997));

        assertEquals("订单不存在", ex.getMessage());
    }

    @Test
    void utOs14_rejectOrder_shouldUpdateStateToReject() {
        Order order = buildOrder(14, "u1001", 1, OrderService.STATE_WAIT, 2, 120);
        when(orderDao.findByOrderID(14)).thenReturn(order);

        orderService.rejectOrder(14);

        verify(orderDao).updateState(OrderService.STATE_REJECT, 14);
    }

    @Test
    void utOs15_rejectOrder_notFound_shouldThrowRuntimeException() {
        when(orderDao.findByOrderID(996)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> orderService.rejectOrder(996));

        assertEquals("订单不存在", ex.getMessage());
    }

    @Test
    void utOs16_findNoAuditOrder_shouldReturnOnlyNoAuditOrders() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> page = new PageImpl<>(Collections.singletonList(buildOrder(15, "u1001", 1, OrderService.STATE_NO_AUDIT, 2, 120)));
        when(orderDao.findAllByState(OrderService.STATE_NO_AUDIT, pageable)).thenReturn(page);

        Page<Order> result = orderService.findNoAuditOrder(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(OrderService.STATE_NO_AUDIT, result.getContent().get(0).getState());
        verify(orderDao).findAllByState(OrderService.STATE_NO_AUDIT, pageable);
    }

    @Test
    void utOs17_findAuditOrder_shouldReturnStateWaitOrFinish() {
        List<Order> orders = Arrays.asList(
                buildOrder(20, "u1001", 1, OrderService.STATE_WAIT, 2, 120),
                buildOrder(21, "u1001", 1, OrderService.STATE_FINISH, 2, 120)
        );
        when(orderDao.findAudit(OrderService.STATE_WAIT, OrderService.STATE_FINISH)).thenReturn(orders);

        List<Order> result = orderService.findAuditOrder();

        assertEquals(2, result.size());
        verify(orderDao).findAudit(OrderService.STATE_WAIT, OrderService.STATE_FINISH);
    }

    private Order buildOrder(int orderID, String userID, int venueID, int state, int hours, int total) {
        Order order = new Order();
        order.setOrderID(orderID);
        order.setUserID(userID);
        order.setVenueID(venueID);
        order.setState(state);
        order.setHours(hours);
        order.setTotal(total);
        order.setOrderTime(LocalDateTime.of(2026, 4, 23, 12, 0, 0));
        order.setStartTime(LocalDateTime.of(2026, 4, 24, 9, 0, 0));
        return order;
    }

    private Venue buildVenue(int venueID, String name, int price) {
        Venue venue = new Venue();
        venue.setVenueID(venueID);
        venue.setVenueName(name);
        venue.setPrice(price);
        return venue;
    }
}
