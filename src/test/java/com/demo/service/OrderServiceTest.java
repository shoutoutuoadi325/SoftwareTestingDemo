package com.demo.service;

import com.demo.dao.OrderDao;
import com.demo.dao.VenueDao;
import com.demo.entity.Order;
import com.demo.entity.Venue;
import com.demo.service.impl.OrderServiceImpl;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
    @Tag("P0")
    void utOs01_findById_shouldReturnOrder() {
        Order order = new Order(1, "test", 16, 1, LocalDateTime.now(), LocalDateTime.now().plusDays(1), 2, 1000);
        when(orderDao.getOne(1)).thenReturn(order);

        Order result = orderService.findById(1);

        assertSame(order, result);
    }

    @Test
    @Tag("P0")
    void utOs02_findDateOrder_shouldReturnOrdersInRange() {
        LocalDateTime start = LocalDateTime.of(2026, 4, 20, 0, 0);
        LocalDateTime end = start.plusDays(1);
        List<Order> orders = Arrays.asList(
                new Order(1, "u", 16, 1, LocalDateTime.now(), start.plusHours(1), 2, 1000)
        );
        when(orderDao.findByVenueIDAndStartTimeIsBetween(16, start, end)).thenReturn(orders);

        List<Order> result = orderService.findDateOrder(16, start, end);

        assertEquals(1, result.size());
    }

    @Test
    @Tag("P0")
    void utOs03_findUserOrder_shouldReturnOrdersByUser() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Order> page = new PageImpl<>(Arrays.asList(
                new Order(1, "test", 16, 1, LocalDateTime.now(), LocalDateTime.now().plusDays(1), 2, 1000)
        ));
        when(orderDao.findAllByUserID("test", pageable)).thenReturn(page);

        Page<Order> result = orderService.findUserOrder("test", pageable);

        assertEquals(1, result.getContent().size());
        assertEquals("test", result.getContent().get(0).getUserID());
    }

    @Test
    @Tag("P0")
    void utOs04_submit_shouldCreateNoAuditOrderWithCorrectTotal() {
        Venue venue = new Venue(16, "场馆2", "d", 500, "", "上海", "09:00", "18:00");
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 22, 10, 0);
        when(venueDao.findByVenueName("场馆2")).thenReturn(venue);

        orderService.submit("场馆2", startTime, 2, "test");

        verify(orderDao).save(any(Order.class));
    }

    @Test
    @Tag("P0")
    void utOs05_updateOrder_shouldResetStateAndRecalculateTotal() {
        Venue venue = new Venue(16, "场馆2", "d", 500, "", "上海", "09:00", "18:00");
        Order order = new Order(1, "test", 2, 2, LocalDateTime.now(), LocalDateTime.now().plusDays(1), 1, 200);
        LocalDateTime newStart = LocalDateTime.of(2026, 5, 1, 10, 0);
        when(venueDao.findByVenueName("场馆2")).thenReturn(venue);
        when(orderDao.findByOrderID(1)).thenReturn(order);

        orderService.updateOrder(1, "场馆2", newStart, 3, "test");

        assertEquals(OrderService.STATE_NO_AUDIT, order.getState());
        assertEquals(3, order.getHours());
        assertEquals(16, order.getVenueID());
        assertEquals("test", order.getUserID());
        assertEquals(1500, order.getTotal());
        assertNotNull(order.getOrderTime());
        verify(orderDao).save(order);
    }

    @Test
    @Tag("P0")
    void utOs06_delOrder_shouldDeleteById() {
        orderService.delOrder(1);

        verify(orderDao).deleteById(1);
    }

    @Test
    @Tag("P0")
    void utOs07_confirmOrder_shouldUpdateStateToWait() {
        Order order = new Order(1, "test", 16, 1, LocalDateTime.now(), LocalDateTime.now().plusDays(1), 2, 1000);
        when(orderDao.findByOrderID(1)).thenReturn(order);

        orderService.confirmOrder(1);

        verify(orderDao).updateState(OrderService.STATE_WAIT, 1);
    }

    @Test
    @Tag("P0")
    void utOs08_finishOrder_shouldUpdateStateToFinish() {
        Order order = new Order(1, "test", 16, 2, LocalDateTime.now(), LocalDateTime.now().plusDays(1), 2, 1000);
        when(orderDao.findByOrderID(1)).thenReturn(order);

        orderService.finishOrder(1);

        verify(orderDao).updateState(OrderService.STATE_FINISH, 1);
    }

    @Test
    @Tag("P0")
    void utOs09_rejectOrder_shouldUpdateStateToReject() {
        Order order = new Order(1, "test", 16, 1, LocalDateTime.now(), LocalDateTime.now().plusDays(1), 2, 1000);
        when(orderDao.findByOrderID(1)).thenReturn(order);

        orderService.rejectOrder(1);

        verify(orderDao).updateState(OrderService.STATE_REJECT, 1);
    }

    @Test
    @Tag("P0")
    void utOs10_confirmOrder_notFound_shouldThrowRuntimeException() {
        when(orderDao.findByOrderID(-1)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> orderService.confirmOrder(-1));

        assertEquals("订单不存在", ex.getMessage());
    }

    @Test
    @Tag("P1")
    void utOs11_findNoAuditOrder_shouldReturnOnlyNoAuditOrders() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> page = new PageImpl<>(Arrays.asList(
                new Order(1, "u", 16, OrderService.STATE_NO_AUDIT, LocalDateTime.now(), LocalDateTime.now().plusDays(1), 2, 1000)
        ));
        when(orderDao.findAllByState(OrderService.STATE_NO_AUDIT, pageable)).thenReturn(page);

        Page<Order> result = orderService.findNoAuditOrder(pageable);

        assertEquals(1, result.getContent().size());
        assertEquals(OrderService.STATE_NO_AUDIT, result.getContent().get(0).getState());
    }

    @Test
    @Tag("P1")
    void utOs12_findAuditOrder_shouldReturnStateWaitOrFinish() {
        List<Order> orders = Arrays.asList(
                new Order(1, "u", 16, OrderService.STATE_WAIT, LocalDateTime.now(), LocalDateTime.now().plusDays(1), 2, 1000),
                new Order(2, "u", 16, OrderService.STATE_FINISH, LocalDateTime.now(), LocalDateTime.now().plusDays(1), 2, 1000)
        );
        when(orderDao.findAudit(OrderService.STATE_WAIT, OrderService.STATE_FINISH)).thenReturn(orders);

        List<Order> result = orderService.findAuditOrder();

        assertEquals(2, result.size());
        result.forEach(order -> {
            assertEquals(true, order.getState() == OrderService.STATE_WAIT || order.getState() == OrderService.STATE_FINISH);
        });
    }
}
