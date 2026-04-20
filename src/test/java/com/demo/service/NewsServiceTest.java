package com.demo.service;

import com.demo.dao.NewsDao;
import com.demo.entity.News;
import com.demo.service.impl.NewsServiceImpl;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsServiceTest {

    @Mock
    private NewsDao newsDao;

    @InjectMocks
    private NewsServiceImpl newsService;

    @Test
    @Tag("P1")
    void utNs01_findAll_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<News> page = new PageImpl<>(Arrays.asList(
                new News(1, "t1", "c1", LocalDateTime.now()),
                new News(2, "t2", "c2", LocalDateTime.now())
        ));
        when(newsDao.findAll(pageable)).thenReturn(page);

        Page<News> result = newsService.findAll(pageable);

        assertEquals(2, result.getContent().size());
    }

    @Test
    @Tag("P0")
    void utNs02_findById_shouldReturnNews() {
        News news = new News(12, "title", "content", LocalDateTime.now());
        when(newsDao.getOne(12)).thenReturn(news);

        News result = newsService.findById(12);

        assertSame(news, result);
    }

    @Test
    @Tag("P0")
    void utNs03_create_shouldReturnNewsID() {
        News news = new News(0, "new", "content", LocalDateTime.now());
        News saved = new News(55, "new", "content", LocalDateTime.now());
        when(newsDao.save(news)).thenReturn(saved);

        int result = newsService.create(news);

        assertEquals(55, result);
    }

    @Test
    @Tag("P0")
    void utNs04_update_shouldSaveNews() {
        News news = new News(12, "new title", "new content", LocalDateTime.now());

        newsService.update(news);

        verify(newsDao).save(news);
    }

    @Test
    @Tag("P0")
    void utNs05_delById_shouldDeleteNews() {
        newsService.delById(12);

        verify(newsDao).deleteById(12);
    }
}
