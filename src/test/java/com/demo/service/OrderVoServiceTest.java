package com.demo.service;

import com.demo.dao.OrderDao;
import com.demo.dao.VenueDao;
import com.demo.entity.Order;
import com.demo.entity.Venue;
import com.demo.entity.vo.OrderVo;
import com.demo.service.impl.OrderVoServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderVoServiceTest {

    @Mock
    private OrderDao orderDao;

    @Mock
    private VenueDao venueDao;

    @InjectMocks
    private OrderVoServiceImpl orderVoService;

    @Test
    void utOvs01_returnOrderVoByOrderID_shouldContainVenueName() {
        Order order = buildOrder(1, "u1001", 10, OrderService.STATE_WAIT);
        Venue venue = buildVenue(10, "羽毛球馆");
        when(orderDao.findByOrderID(1)).thenReturn(order);
        when(venueDao.findByVenueID(10)).thenReturn(venue);

        OrderVo result = orderVoService.returnOrderVoByOrderID(1);

        assertEquals(1, result.getOrderID());
        assertEquals("羽毛球馆", result.getVenueName());
        assertEquals("u1001", result.getUserID());
    }

    @Test
    void utOvs02_returnVo_shouldReturnMappedListWithSameSize() {
        Order order1 = buildOrder(1, "u1001", 10, OrderService.STATE_WAIT);
        Order order2 = buildOrder(2, "u1002", 11, OrderService.STATE_FINISH);
        when(orderDao.findByOrderID(1)).thenReturn(order1);
        when(orderDao.findByOrderID(2)).thenReturn(order2);
        when(venueDao.findByVenueID(10)).thenReturn(buildVenue(10, "羽毛球馆"));
        when(venueDao.findByVenueID(11)).thenReturn(buildVenue(11, "网球馆"));

        List<OrderVo> result = orderVoService.returnVo(Arrays.asList(order1, order2));

        assertEquals(2, result.size());
        assertEquals("羽毛球馆", result.get(0).getVenueName());
        assertEquals("网球馆", result.get(1).getVenueName());
    }

    @Test
    void utOvs03_returnOrderVoByOrderID_whenOrderMissing_shouldThrowIllegalArgumentException() {
        when(orderDao.findByOrderID(404)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> orderVoService.returnOrderVoByOrderID(404));
    }

    private Order buildOrder(int orderID, String userID, int venueID, int state) {
        Order order = new Order();
        order.setOrderID(orderID);
        order.setUserID(userID);
        order.setVenueID(venueID);
        order.setState(state);
        order.setHours(2);
        order.setTotal(100);
        order.setOrderTime(LocalDateTime.of(2026, 4, 23, 10, 0, 0));
        order.setStartTime(LocalDateTime.of(2026, 4, 24, 9, 0, 0));
        return order;
    }

    private Venue buildVenue(int venueID, String venueName) {
        Venue venue = new Venue();
        venue.setVenueID(venueID);
        venue.setVenueName(venueName);
        return venue;
    }
}
