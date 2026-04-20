package com.demo.service;

import com.demo.dao.OrderDao;
import com.demo.dao.VenueDao;
import com.demo.entity.Order;
import com.demo.entity.Venue;
import com.demo.entity.vo.OrderVo;
import com.demo.service.impl.OrderVoServiceImpl;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    @Tag("P1")
    void utOvs01_returnOrderVoByOrderID_shouldContainVenueName() {
        Order order = new Order(1, "test", 16, 2, LocalDateTime.now(), LocalDateTime.now().plusDays(1), 2, 1000);
        Venue venue = new Venue(16, "场馆2", "d", 500, "", "上海", "09:00", "18:00");
        when(orderDao.findByOrderID(1)).thenReturn(order);
        when(venueDao.findByVenueID(16)).thenReturn(venue);

        OrderVo result = orderVoService.returnOrderVoByOrderID(1);

        assertEquals(1, result.getOrderID());
        assertEquals("场馆2", result.getVenueName());
        assertEquals(16, result.getVenueID());
    }

    @Test
    @Tag("P1")
    void utOvs02_returnVo_shouldReturnMappedListWithSameSize() {
        Order o1 = new Order(1, "u", 16, 1, LocalDateTime.now(), LocalDateTime.now().plusDays(1), 1, 500);
        Order o2 = new Order(2, "u", 17, 2, LocalDateTime.now(), LocalDateTime.now().plusDays(1), 2, 600);
        when(orderDao.findByOrderID(1)).thenReturn(o1);
        when(orderDao.findByOrderID(2)).thenReturn(o2);
        when(venueDao.findByVenueID(16)).thenReturn(new Venue(16, "场馆2", "", 500, "", "", "", ""));
        when(venueDao.findByVenueID(17)).thenReturn(new Venue(17, "场馆3", "", 300, "", "", "", ""));

        List<OrderVo> result = orderVoService.returnVo(Arrays.asList(o1, o2));

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getOrderID());
        assertEquals(2, result.get(1).getOrderID());
    }
}
