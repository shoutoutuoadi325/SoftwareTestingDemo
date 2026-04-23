package com.demo.controller;

import com.demo.controller.user.NewsController;
import com.demo.controller.user.VenueController;
import com.demo.entity.Message;
import com.demo.entity.News;
import com.demo.entity.Venue;
import com.demo.entity.vo.MessageVo;
import com.demo.service.MessageService;
import com.demo.service.MessageVoService;
import com.demo.service.NewsService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class NewsVenueIndexIntegrationTest {

    @Mock
    private NewsService newsService;

    @Mock
    private VenueService venueService;

    @Mock
    private MessageVoService messageVoService;

    @Mock
    private MessageService messageService;

    @InjectMocks
    private IndexController indexController;

    @InjectMocks
    private NewsController newsController;

    @InjectMocks
    private VenueController venueController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(indexController, newsController, venueController)
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
    void itHome01_index_shouldContainHomeModelAttributes() throws Exception {
        Page<Venue> venuePage = new PageImpl<>(Collections.singletonList(buildVenue(1, "venue-1")), PageRequest.of(0, 5), 1);
        Page<News> newsPage = new PageImpl<>(Collections.singletonList(buildNews(1, "news-1")), PageRequest.of(0, 5), 1);
        Page<Message> messagePage = new PageImpl<>(Collections.singletonList(buildMessage(1, "u1001")), PageRequest.of(0, 5), 1);

        when(venueService.findAll(any())).thenReturn(venuePage);
        when(newsService.findAll(any())).thenReturn(newsPage);
        when(messageService.findPassState(any())).thenReturn(messagePage);
        when(messageVoService.returnVo(any())).thenReturn(Collections.singletonList(buildMessageVo(1, "u1001")));

        mockMvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("news_list"))
                .andExpect(model().attributeExists("venue_list"))
                .andExpect(model().attributeExists("message_list"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void itHome02_adminIndex_shouldReturnAdminIndexView() throws Exception {
        mockMvc.perform(get("/admin_index"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/admin_index"));
    }

    @Test
    void itNews01_newsListPage_shouldReturnViewAndTotal() throws Exception {
        Page<News> newsPage = new PageImpl<>(Arrays.asList(buildNews(1, "n1"), buildNews(2, "n2")), PageRequest.of(0, 5), 8);
        when(newsService.findAll(any())).thenReturn(newsPage);

        mockMvc.perform(get("/news_list"))
                .andExpect(status().isOk())
                .andExpect(view().name("news_list"))
                .andExpect(model().attributeExists("news_list"))
                .andExpect(model().attribute("total", 2));
    }

    @Test
    void itNews02_newsGetList_shouldReturnPageJson() throws Exception {
        Page<News> newsPage = new PageImpl<>(Collections.singletonList(buildNews(3, "n3")), PageRequest.of(0, 5), 1);
        when(newsService.findAll(any())).thenReturn(newsPage);

        mockMvc.perform(get("/news/getNewsList").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].newsID").value(3));
    }

    @Test
    void itNews03_newsDetail_shouldReturnNewsView() throws Exception {
        when(newsService.findById(5)).thenReturn(buildNews(5, "detail"));

        mockMvc.perform(get("/news").param("newsID", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("news"))
                .andExpect(model().attributeExists("news"));
    }

    @Test
    void itNews04_newsGetListPageZero_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/news/getNewsList").param("page", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void itVenue01_venueListPage_shouldReturnViewAndTotal() throws Exception {
        Page<Venue> venuePage = new PageImpl<>(Collections.singletonList(buildVenue(1, "v1")), PageRequest.of(0, 5), 6);
        when(venueService.findAll(any())).thenReturn(venuePage);

        mockMvc.perform(get("/venue_list"))
                .andExpect(status().isOk())
                .andExpect(view().name("venue_list"))
                .andExpect(model().attributeExists("venue_list"))
                .andExpect(model().attribute("total", 2));
    }

    @Test
    void itVenue02_venueGetList_shouldReturnPageJson() throws Exception {
        Page<Venue> venuePage = new PageImpl<>(Collections.singletonList(buildVenue(2, "v2")), PageRequest.of(0, 5), 1);
        when(venueService.findAll(any())).thenReturn(venuePage);

        mockMvc.perform(get("/venuelist/getVenueList").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].venueID").value(2));
    }

    @Test
    void itVenue03_venueDetail_shouldReturnVenueView() throws Exception {
        when(venueService.findByVenueID(4)).thenReturn(buildVenue(4, "detail"));

        mockMvc.perform(get("/venue").param("venueID", "4"))
                .andExpect(status().isOk())
                .andExpect(view().name("venue"))
                .andExpect(model().attributeExists("venue"));
    }

    @Test
    void itVenue04_venueGetListPageZero_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/venuelist/getVenueList").param("page", "0"))
                .andExpect(status().isBadRequest());
    }

    private News buildNews(int id, String title) {
        News news = new News();
        news.setNewsID(id);
        news.setTitle(title);
        news.setContent("content");
        news.setTime(LocalDateTime.of(2026, 4, 23, 10, 0, 0));
        return news;
    }

    private Venue buildVenue(int id, String name) {
        Venue venue = new Venue();
        venue.setVenueID(id);
        venue.setVenueName(name);
        venue.setDescription("desc");
        venue.setPrice(80);
        venue.setPicture("");
        venue.setAddress("addr");
        venue.setOpen_time("08:00");
        venue.setClose_time("22:00");
        return venue;
    }

    private Message buildMessage(int id, String userID) {
        Message message = new Message();
        message.setMessageID(id);
        message.setUserID(userID);
        message.setContent("msg");
        message.setState(2);
        message.setTime(LocalDateTime.of(2026, 4, 23, 11, 0, 0));
        return message;
    }

    private MessageVo buildMessageVo(int id, String userID) {
        MessageVo vo = new MessageVo();
        vo.setMessageID(id);
        vo.setUserID(userID);
        vo.setContent("msg");
        vo.setState(2);
        vo.setUserName("name");
        vo.setPicture("pic.png");
        vo.setTime(LocalDateTime.of(2026, 4, 23, 11, 0, 0));
        return vo;
    }
}
