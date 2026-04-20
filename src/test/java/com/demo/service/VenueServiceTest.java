package com.demo.service;

import com.demo.dao.VenueDao;
import com.demo.entity.Venue;
import com.demo.service.impl.VenueServiceImpl;
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

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VenueServiceTest {

    @Mock
    private VenueDao venueDao;

    @InjectMocks
    private VenueServiceImpl venueService;

    @Test
    @Tag("P0")
    void utVs01_findByVenueID_shouldReturnVenue() {
        Venue venue = new Venue(16, "场馆2", "desc", 500, "", "上海", "09:00", "18:00");
        when(venueDao.getOne(16)).thenReturn(venue);

        Venue result = venueService.findByVenueID(16);

        assertSame(venue, result);
    }

    @Test
    @Tag("P0")
    void utVs02_findByVenueName_shouldReturnVenue() {
        Venue venue = new Venue(16, "场馆2", "desc", 500, "", "上海", "09:00", "18:00");
        when(venueDao.findByVenueName("场馆2")).thenReturn(venue);

        Venue result = venueService.findByVenueName("场馆2");

        assertSame(venue, result);
    }

    @Test
    @Tag("P1")
    void utVs03_findAllPageable_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Venue> page = new PageImpl<>(Arrays.asList(
                new Venue(1, "v1", "d1", 100, "", "a", "09:00", "20:00"),
                new Venue(2, "v2", "d2", 200, "", "b", "09:00", "20:00")
        ));
        when(venueDao.findAll(pageable)).thenReturn(page);

        Page<Venue> result = venueService.findAll(pageable);

        assertEquals(2, result.getContent().size());
    }

    @Test
    @Tag("P1")
    void utVs04_findAll_shouldReturnList() {
        List<Venue> venues = Arrays.asList(
                new Venue(1, "v1", "d1", 100, "", "a", "09:00", "20:00"),
                new Venue(2, "v2", "d2", 200, "", "b", "09:00", "20:00")
        );
        when(venueDao.findAll()).thenReturn(venues);

        List<Venue> result = venueService.findAll();

        assertEquals(2, result.size());
    }

    @Test
    @Tag("P0")
    void utVs05_create_shouldReturnVenueID() {
        Venue venue = new Venue(0, "new", "d", 123, "", "a", "09:00", "20:00");
        Venue saved = new Venue(99, "new", "d", 123, "", "a", "09:00", "20:00");
        when(venueDao.save(venue)).thenReturn(saved);

        int result = venueService.create(venue);

        assertEquals(99, result);
    }

    @Test
    @Tag("P0")
    void utVs06_update_shouldSaveVenue() {
        Venue venue = new Venue(16, "场馆2", "new", 600, "", "上海", "09:00", "18:00");

        venueService.update(venue);

        verify(venueDao).save(venue);
    }

    @Test
    @Tag("P0")
    void utVs07_delById_shouldDeleteVenue() {
        venueService.delById(16);

        verify(venueDao).deleteById(16);
    }

    @Test
    @Tag("P1")
    void utVs08_countVenueName_shouldReturnCount() {
        when(venueDao.countByVenueName("场馆2")).thenReturn(1);
        when(venueDao.countByVenueName("ut_venue_x")).thenReturn(0);

        int existing = venueService.countVenueName("场馆2");
        int notExisting = venueService.countVenueName("ut_venue_x");

        assertEquals(1, existing);
        assertEquals(0, notExisting);
    }
}
