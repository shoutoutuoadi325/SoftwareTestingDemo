package com.demo.service;

import com.demo.dao.NewsDao;
import com.demo.entity.News;
import com.demo.service.impl.NewsServiceImpl;
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
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsServiceTest {

    @Mock
    private NewsDao newsDao;

    @InjectMocks
    private NewsServiceImpl newsService;

    @Test
    void utNs01_findAll_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<News> page = new PageImpl<>(Collections.singletonList(buildNews(1, "title")));
        when(newsDao.findAll(pageable)).thenReturn(page);

        Page<News> result = newsService.findAll(pageable);

        assertEquals(1, result.getTotalElements());
        verify(newsDao).findAll(pageable);
    }

    @Test
    void utNs02_findById_shouldReturnNews() {
        News news = buildNews(2, "news2");
        when(newsDao.getOne(2)).thenReturn(news);

        News result = newsService.findById(2);

        assertEquals("news2", result.getTitle());
        verify(newsDao).getOne(2);
    }

    @Test
    void utNs03_create_shouldReturnNewsID() {
        News news = buildNews(3, "new");
        when(newsDao.save(news)).thenReturn(news);

        int id = newsService.create(news);

        assertEquals(3, id);
        verify(newsDao).save(news);
    }

    @Test
    void utNs04_update_shouldSaveNews() {
        News news = buildNews(4, "update");

        newsService.update(news);

        verify(newsDao).save(news);
    }

    @Test
    void utNs05_delById_shouldDeleteNews() {
        newsService.delById(5);

        verify(newsDao).deleteById(5);
    }

    private News buildNews(int newsID, String title) {
        News news = new News();
        news.setNewsID(newsID);
        news.setTitle(title);
        news.setContent("content");
        news.setTime(LocalDateTime.of(2026, 4, 23, 10, 0, 0));
        return news;
    }
}
