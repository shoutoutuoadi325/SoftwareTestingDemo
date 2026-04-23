package com.demo.service;

import com.demo.dao.VenueDao;
import com.demo.entity.Venue;
import com.demo.service.impl.VenueServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VenueServiceTest {

    @Mock
    private VenueDao venueDao;

    @InjectMocks
    private VenueServiceImpl venueService;

    @Test
    void utVs01_findByVenueID_shouldReturnVenue() {
        Venue venue = buildVenue(1, "羽毛球馆", 80);
        when(venueDao.getOne(1)).thenReturn(venue);

        Venue result = venueService.findByVenueID(1);

        assertEquals("羽毛球馆", result.getVenueName());
        verify(venueDao).getOne(1);
    }

    @Test
    void utVs02_findByVenueName_shouldReturnVenue() {
        Venue venue = buildVenue(2, "游泳馆", 120);
        when(venueDao.findByVenueName("游泳馆")).thenReturn(venue);

        Venue result = venueService.findByVenueName("游泳馆");

        assertEquals(2, result.getVenueID());
        verify(venueDao).findByVenueName("游泳馆");
    }

    @Test
    void utVs03_findAllPageable_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Venue> page = new PageImpl<>(Collections.singletonList(buildVenue(1, "羽毛球馆", 80)));
        when(venueDao.findAll(pageable)).thenReturn(page);

        Page<Venue> result = venueService.findAll(pageable);

        assertEquals(1, result.getTotalElements());
        verify(venueDao).findAll(pageable);
    }

    @Test
    void utVs04_findAll_shouldReturnList() {
        List<Venue> venues = Arrays.asList(buildVenue(1, "羽毛球馆", 80), buildVenue(2, "网球馆", 90));
        when(venueDao.findAll()).thenReturn(venues);

        List<Venue> result = venueService.findAll();

        assertEquals(2, result.size());
        verify(venueDao).findAll();
    }

    @Test
    void utVs05_create_shouldReturnVenueID() {
        Venue venue = buildVenue(10, "新馆", 100);
        when(venueDao.save(venue)).thenReturn(venue);

        int venueID = venueService.create(venue);

        assertEquals(10, venueID);
        verify(venueDao).save(venue);
    }

    @Test
    void utVs06_update_shouldSaveVenue() {
        Venue venue = buildVenue(1, "羽毛球馆2", 100);

        venueService.update(venue);

        verify(venueDao).save(venue);
    }

    @Test
    void utVs07_delById_shouldDeleteVenue() {
        venueService.delById(5);

        verify(venueDao).deleteById(5);
    }

    @Test
    void utVs08_countVenueName_shouldReturnCount() {
        when(venueDao.countByVenueName("羽毛球馆")).thenReturn(1);

        int count = venueService.countVenueName("羽毛球馆");

        assertEquals(1, count);
        verify(venueDao).countByVenueName("羽毛球馆");
    }

    private Venue buildVenue(int venueID, String venueName, int price) {
        Venue venue = new Venue();
        venue.setVenueID(venueID);
        venue.setVenueName(venueName);
        venue.setDescription("desc");
        venue.setAddress("addr");
        venue.setPrice(price);
        venue.setPicture("");
        venue.setOpen_time("08:00");
        venue.setClose_time("22:00");
        return venue;
    }
}
